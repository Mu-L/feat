package tech.smartboot.feat.core.client.impl;

import org.smartboot.socket.transport.AioSession;
import tech.smartboot.feat.core.client.AbstractResponse;
import tech.smartboot.feat.core.client.BodySteaming;
import tech.smartboot.feat.core.client.HttpResponse;
import tech.smartboot.feat.core.common.HeaderValue;
import tech.smartboot.feat.core.common.enums.HeaderNameEnum;
import tech.smartboot.feat.core.common.exception.FeatException;
import tech.smartboot.feat.core.common.utils.Constant;
import tech.smartboot.feat.core.common.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

public class HttpResponseImpl extends AbstractResponse implements HttpResponse {
    private static final int STATE_CHUNK_LENGTH = 1;
    private static final int STATE_CHUNK_CONTENT = 1 << 1;
    private static final int STATE_CHUNK_END = 1 << 2;
    private static final int STATE_CONTENT_LENGTH = 1 << 3;
    private static final int STATE_FINISH = 1 << 4;
    private static final int STATE_GZIP = 0x80;
    protected BodySteaming steaming;
    private int state;

    private long remaining;
    /**
     * body内容
     */
    private String body;


    public HttpResponseImpl(AioSession session, CompletableFuture future) {
        super(session, future);
    }

    public String body() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public void reset() {

    }

    @Override
    public void onHeaderComplete() throws IOException {
        String transferEncoding = getHeader(HeaderNameEnum.TRANSFER_ENCODING.getName());
        if (StringUtils.equals(transferEncoding, HeaderValue.TransferEncoding.CHUNKED)) {
            state = STATE_CHUNK_LENGTH;
        } else if (getContentLength() > 0) {
            state = STATE_CONTENT_LENGTH;
            remaining = getContentLength();
        } else {
            state = STATE_FINISH;
        }

        if (StringUtils.equals(HeaderValue.ContentEncoding.GZIP, getHeader(HeaderNameEnum.CONTENT_ENCODING.getName()))) {
            state = STATE_GZIP | state;
            steaming = new GzipBodySteaming(steaming);
        }
    }

    @Override
    public void onBodyStream(ByteBuffer buffer) {
        try {
            switch (state & ~STATE_GZIP) {
                case STATE_CHUNK_LENGTH: {
                    int length = StringUtils.scanUntilAndTrim(buffer, Constant.LF);
                    if (length < 0) {
                        return;
                    }
                    if (length == 1) {
                        state = STATE_FINISH;
                    } else {
                        remaining = StringUtils.convertHexString(buffer, buffer.position() - length - 1, length - 1);
                        if (remaining != 0) {
                            state = STATE_CHUNK_CONTENT;
                        }
                    }
                    onBodyStream(buffer);
                }
                break;
                case STATE_CHUNK_CONTENT: {
                    int length = Integer.min(buffer.remaining(), (int) remaining);
                    if (length == 0) {
                        return;
                    }
                    byte[] bytes = new byte[length];
                    buffer.get(bytes);
                    remaining -= length;
                    steaming.stream(this, bytes);
                    if (remaining == 0) {
                        state = STATE_CHUNK_END;
                    }
                    onBodyStream(buffer);
                }
                break;
                case STATE_CHUNK_END: {
                    if (buffer.remaining() < 2) {
                        return;
                    }
                    if (buffer.get() == Constant.CR && buffer.get() == Constant.LF) {
                        state = STATE_CHUNK_LENGTH;
                        onBodyStream(buffer);
                    } else {
                        throw new IllegalStateException();
                    }
                }
                break;
                case STATE_CONTENT_LENGTH: {
                    int length = Integer.min(buffer.remaining(), (int) remaining);
                    if (length == 0) {
                        return;
                    }
                    byte[] bytes = new byte[length];
                    buffer.get(bytes);
                    remaining -= length;
                    steaming.stream(this, bytes);
                    if (remaining == 0) {
                        state = STATE_FINISH;
                    }
                    onBodyStream(buffer);
                }
                break;
                case STATE_FINISH:
                    finishDecode();
                    break;
                default:
                    throw new IllegalStateException();
            }
        } catch (Exception e) {
            throw new FeatException(e);
        }
    }

    public void finishDecode() throws IOException {
        steaming.end(this);
        getFuture().complete(this);
    }


    class GzipBodySteaming implements BodySteaming {
        ByteBuffer buffer;
        private GZIPInputStream gzipInputStream;
        private final BodySteaming bodySteaming;

        public GzipBodySteaming(BodySteaming bodySteaming) throws IOException {
            this.bodySteaming = bodySteaming;
        }

        @Override
        public void stream(HttpResponse response, byte[] data) throws IOException {
            if (buffer != null && buffer.remaining() > 0) {
                buffer.compact();
                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.remaining() + data.length);
                newBuffer.put(buffer);
                newBuffer.put(data);
                newBuffer.flip();
                buffer = newBuffer;
                buffer.compact();
            } else {
                buffer = ByteBuffer.wrap(data);
            }
            if (gzipInputStream == null) {
                gzipInputStream = new GZIPInputStream(new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return (buffer == null ? -1 : buffer.get()) & 0xFF;
                    }

                    @Override
                    public int read(byte[] b, int off, int len) throws IOException {
                        if (buffer == null) {
                            return -1;
                        }
                        int size = Math.min(buffer.remaining(), len);
                        buffer.get(b, off, size);
                        return size;
                    }

                    @Override
                    public int available() throws IOException {
                        return buffer == null ? 0 : buffer.remaining();
                    }
                }) {
                    @Override
                    public int available() throws IOException {
                        return buffer == null ? 0 : buffer.remaining();
                    }
                };
            }
            byte[] b = new byte[4096];
            int n;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (buffer.remaining() > 100 && (n = gzipInputStream.read(b)) > 0) {
                bos.write(b, 0, n);
            }
            bodySteaming.stream(response, bos.toByteArray());
        }

        @Override
        public void end(HttpResponse response) throws IOException {
            if (gzipInputStream.available() > 0) {
                byte[] bytes = new byte[4096];
                int n;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                while (buffer.hasRemaining() && (n = gzipInputStream.read(bytes)) > 0) {
                    bos.write(bytes, 0, n);
                }
                bodySteaming.stream(response, bos.toByteArray());
            }
            bodySteaming.end(response);
        }
    }

    public void setSteaming(BodySteaming steaming) {
        this.steaming = steaming;
    }
}
