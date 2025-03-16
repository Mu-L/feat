/*
 *  Copyright (C) [2024] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.feat.fileserver;

import tech.smartboot.feat.core.common.HeaderName;
import tech.smartboot.feat.core.common.HttpMethod;
import tech.smartboot.feat.core.common.HttpStatus;
import tech.smartboot.feat.core.common.exception.FeatException;
import tech.smartboot.feat.core.common.exception.HttpException;
import tech.smartboot.feat.core.common.io.FeatOutputStream;
import tech.smartboot.feat.core.common.logging.Logger;
import tech.smartboot.feat.core.common.logging.LoggerFactory;
import tech.smartboot.feat.core.common.utils.DateUtils;
import tech.smartboot.feat.core.common.utils.Mimetypes;
import tech.smartboot.feat.core.common.utils.StringUtils;
import tech.smartboot.feat.core.server.HttpHandler;
import tech.smartboot.feat.core.server.HttpRequest;
import tech.smartboot.feat.core.server.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author 三刀
 * @version v1.0.0
 */
public class HttpStaticResourceHandler implements HttpHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpStaticResourceHandler.class);
    private final Date lastModifyDate = new Date(System.currentTimeMillis() / 1000 * 1000);

    private final String lastModifyDateFormat = DateUtils.formatRFC1123(lastModifyDate);
    private final File baseDir;
    private final FileServerOptions options;
    private String classPath;

    public HttpStaticResourceHandler(FileServerOptions options) {
        try {
            this.options = options;
            if (options.baseDir().startsWith("classpath:")) {
                this.classPath = options.baseDir().substring("classpath:".length());
                this.baseDir = null;
            } else {
                this.baseDir = new File(options.baseDir()).getCanonicalFile();
                if (!this.baseDir.isDirectory()) {
                    throw new RuntimeException(baseDir + " is not a directory");
                }
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("dir is:{}", this.baseDir.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            throw new FeatException(e);
        }
    }

    public void handle(HttpRequest request, CompletableFuture<Object> completableFuture) throws Throwable {
        String fileName = request.getRequestURI();
        if (StringUtils.endsWith(fileName, "/") && !options.autoIndex()) {
            fileName += "index.html";
        }
        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());

        if (classPath == null) {
            handleFilePath(request, completableFuture, fileName);
        } else {
            handleClassPath(request, completableFuture, fileName);
        }
    }

    public void handleClassPath(HttpRequest request, CompletableFuture<Object> completableFuture, String fileName) throws Throwable {
        HttpResponse response = request.getResponse();

        //304 命中缓存
        String requestModified = request.getHeader(HeaderName.IF_MODIFIED_SINCE);
        if (StringUtils.isNotBlank(requestModified) && lastModifyDate.getTime() <= DateUtils.parseRFC1123(requestModified).getTime()) {
            response.setHttpStatus(HttpStatus.NOT_MODIFIED);
            completableFuture.complete(null);
            return;
        }


        InputStream inputStream = HttpStaticResourceHandler.class.getClassLoader().getResourceAsStream(classPath + fileName);
        if (inputStream == null) {
            fileNotFound(request, response);
            completableFuture.complete(null);
            return;
        }
        completableFuture.whenComplete((r, t) -> {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        });
        response.setHeader(HeaderName.LAST_MODIFIED, lastModifyDateFormat);
        response.setHeader(HeaderName.CONTENT_TYPE, Mimetypes.getInstance().getMimetype(fileName) + "; charset=utf-8");

        Consumer<FeatOutputStream> consumer = new Consumer<FeatOutputStream>() {
            final byte[] bytes = new byte[options.writeBufferSize()];

            @Override
            public void accept(FeatOutputStream featOutputStream) {
                int length;
                try {
                    if ((length = inputStream.read(bytes)) > 0) {
                        featOutputStream.write(bytes, 0, length, this);
                    } else {
                        completableFuture.complete(null);
                    }
                } catch (Throwable throwable) {
                    completableFuture.completeExceptionally(throwable);
                }
            }
        };
        consumer.accept(response.getOutputStream());
    }

    private void handleFilePath(HttpRequest request, CompletableFuture<Object> completableFuture, String fileName) throws Throwable {
        HttpResponse response = request.getResponse();
        File file = new File(baseDir, fileName);

        if (options.autoIndex() && file.isDirectory()) {
            int index = fileName.lastIndexOf('/');
            String path = fileName;
            if (index != -1) {
                path = fileName.substring(0, index);
            }
            if (StringUtils.length(path) <= 1) {
                path = "/";
            }
            response.write("返回上一级：<a href='" + path + "'>&gt;" + fileName + "</a>");
            response.write("<ul>");
            for (File f : Objects.requireNonNull(file.listFiles(File::isDirectory))) {
                if (request.getRequestURI().endsWith("/")) {
                    request.getResponse().write("<li><a href='" + request.getRequestURI() + f.getName() + "'>&gt;\uD83D\uDCC1 &nbsp;" + f.getName() + "</a></li>");
                } else {
                    request.getResponse().write("<li><a href='" + request.getRequestURI() + "/" + f.getName() + "'>&gt;\uD83D\uDCC1 &nbsp;" + f.getName() + "</a></li>");
                }

            }
            for (File f : Objects.requireNonNull(file.listFiles(File::isFile))) {
                if (request.getRequestURI().endsWith("/")) {
                    request.getResponse().write("<li><a href='" + request.getRequestURI() + f.getName() + "'>" + f.getName() + "</a></li>");
                } else {
                    request.getResponse().write("<li><a href='" + request.getRequestURI() + "/" + f.getName() + "'>" + f.getName() + "</a></li>");
                }

            }
            response.write("</ul>");
            completableFuture.complete(null);
            return;
        } else if (file.isDirectory()) {
            file = new File(file, "index.html");
        }

        if (!file.isFile()) {
            fileNotFound(request, response);
            completableFuture.complete(null);
            return;
        }
        //304
        Date lastModifyDate = new Date(file.lastModified() / 1000 * 1000);
        String requestModified = request.getHeader(HeaderName.IF_MODIFIED_SINCE);
        if (StringUtils.isNotBlank(requestModified) && lastModifyDate.getTime() <= DateUtils.parseRFC1123(requestModified).getTime()) {
            response.setHttpStatus(HttpStatus.NOT_MODIFIED);
            completableFuture.complete(null);
            return;
        }


        response.setHeader(HeaderName.LAST_MODIFIED, DateUtils.formatRFC1123(lastModifyDate));
        response.setHeader(HeaderName.CONTENT_TYPE, Mimetypes.getInstance().getMimetype(file) + "; charset=utf-8");
        //HEAD不输出内容
        if (HttpMethod.HEAD.equals(request.getMethod())) {
            completableFuture.complete(null);
            return;
        }

        response.setContentLength((int) file.length());

        FileInputStream fis = new FileInputStream(file);
        completableFuture.whenComplete((o, throwable) -> {
            try {
                fis.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        ByteBuffer buffer = ByteBuffer.allocate(options.writeBufferSize());
        buffer.position(buffer.limit());
        Consumer<FeatOutputStream> consumer = new Consumer<FeatOutputStream>() {
            final AtomicLong readPos = new AtomicLong(0);

            @Override
            public void accept(FeatOutputStream result) {
                try {
                    buffer.compact();
                    int len = fis.getChannel().read(buffer);
                    buffer.flip();
                    if (len == -1) {
                        completableFuture.completeExceptionally(new IOException("EOF"));
                    } else if (readPos.addAndGet(len) >= response.getContentLength()) {
                        response.getOutputStream().transferFrom(buffer, bufferOutputStream -> completableFuture.complete(null));
                    } else {
                        response.getOutputStream().transferFrom(buffer, this);
                    }
                } catch (Throwable throwable) {
                    completableFuture.completeExceptionally(throwable);
                }
            }
        };
        consumer.accept(response.getOutputStream());
    }

    @Override
    public void handle(HttpRequest request) throws Throwable {
        throw new UnsupportedOperationException();
    }

    private void fileNotFound(HttpRequest request, HttpResponse response) throws IOException {
        if (request.getRequestURI().equals("/favicon.ico")) {
            try (InputStream inputStream = HttpStaticResourceHandler.class.getClassLoader().getResourceAsStream("favicon.ico")) {
                if (inputStream == null) {
                    response.setHttpStatus(HttpStatus.NOT_FOUND);
                    return;
                }
                String contentType = Mimetypes.getInstance().getMimetype("favicon.ico");
                response.setHeader(HeaderName.CONTENT_TYPE, contentType + "; charset=utf-8");
                byte[] bytes = new byte[4094];
                int length;
                while ((length = inputStream.read(bytes)) != -1) {
                    response.getOutputStream().write(bytes, 0, length);
                }
            }
            return;
        }
        LOGGER.warn("file: {} not found!", request.getRequestURI());
        response.setHttpStatus(HttpStatus.NOT_FOUND);
        response.setHeader(HeaderName.CONTENT_TYPE, "text/html; charset=utf-8");

        if (!HttpMethod.HEAD.equals(request.getMethod())) {
            throw new HttpException(HttpStatus.NOT_FOUND);
        }
    }

}
