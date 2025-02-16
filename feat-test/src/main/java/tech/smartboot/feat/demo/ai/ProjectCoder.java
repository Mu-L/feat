package tech.smartboot.feat.demo.ai;

import tech.smartboot.feat.Feat;
import tech.smartboot.feat.ai.FeatAI;
import tech.smartboot.feat.ai.ModelMeta;
import tech.smartboot.feat.ai.chat.ChatModel;
import tech.smartboot.feat.ai.chat.entity.ResponseMessage;
import tech.smartboot.feat.ai.chat.entity.StreamResponseCallback;
import tech.smartboot.feat.core.server.HttpResponse;
import tech.smartboot.feat.core.server.upgrade.sse.SSEUpgrade;
import tech.smartboot.feat.core.server.upgrade.sse.SseEmitter;
import tech.smartboot.feat.router.Router;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class ProjectCoder extends BaseChat {
    public static void main(String[] args) throws IOException {
        File file = new File("pages/src/content/docs/server");
        Set<String> ignoreDoc = new HashSet<>();
        ignoreDoc.add("client");
        ignoreDoc.add("cloud");
        ignoreDoc.add("ai");
        StringBuilder docs = new StringBuilder();
        loadFile(file, ignoreDoc, docs);

        Set<String> ignore = new HashSet<>();
        ignore.add("ai");
        ignore.add("common");
        ignore.add("fileserver");
        ignore.add("client");
        ignore.add("upgrade");
        StringBuilder sourceBuilder = new StringBuilder();
        loadSource(new File("feat-core/src/main/java/tech/smartboot/feat/"), ignore, sourceBuilder);

        ChatModel chatModel = FeatAI.chatModel(opts -> {
            opts.model(ModelMeta.GITEE_AI_DeepSeek_R1_Distill_Qwen_32B)
                    .system("你是一名专业的Java程序员，Feat是你设计的一个开源项目。"
//                            + "参考内容为：\n" + docs
                            + "\n 实现源码为：\n" + sourceBuilder
                    )
                    .debug(true)
            ;
        });


        Router router = new Router();
        router.http("/", req -> {
            HttpResponse response = req.getResponse();
            response.setContentType("text/html");
            InputStream inputStream = ProjectCoder.class.getClassLoader().getResourceAsStream("static/project_doc_ai.html");
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                response.write(buffer, 0, length);
            }
        });
        router.http("/chat", req -> {
            req.upgrade(new SSEUpgrade() {
                public void onOpen(SseEmitter sseEmitter) {
                    chatModel.chatStream(req.getParameter("content"), new StreamResponseCallback() {

                        @Override
                        public void onCompletion(ResponseMessage responseMessage) {
                            System.out.println("结束...");
                        }

                        @Override
                        public void onStreamResponse(String content) {
                            System.out.print(content);
                            try {
                                sseEmitter.send(SseEmitter.event().data(toHtml(content)));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            });
        });
        Feat.httpServer(opt -> opt.debug(false)).httpHandler(router).listen(8080);
    }


}
