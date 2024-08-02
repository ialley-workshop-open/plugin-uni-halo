package pro.uhalo.uni.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import pro.uhalo.uni.scheme.QRCodeInfo;
import pro.uhalo.uni.services.QRCodeInfoService;
import pro.uhalo.uni.services.UniHaloService;
import pro.uhalo.uni.utils.TokenManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.endpoint.SimpleFilePart;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.infra.ExternalLinkProcessor;
import run.halo.app.plugin.ReactiveSettingFetcher;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 服务实现
 *
 * @author 小莫唐尼
 */
@Component
@RequiredArgsConstructor
public class UniHaloServiceImpl implements UniHaloService {

    private final QRCodeInfoService qrCodeInfoService;
    private final ReactiveSettingFetcher settingFetcher;
    private final TokenManager tokenManager;
    private final AttachmentService attachmentService;
    private final ExternalLinkProcessor externalLinkProcessor;
    private static final WebClient WEB_CLIENT = WebClient.builder().build();
    public static String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    public static String GET_WXA_CODE_URL = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=";

    /**
     * 获取移动端的所有配置
     *
     * @return 配置
     */
    @Override
    public Mono<Map<String, JsonNode>> getAppConfigs() {
        return this.settingFetcher.getValues();
    }

    /*
     * 根据分组名称获取配置
     *
     * @param groupName 分组名称
     * @return 配置
     */
    @Override
    public Mono<JsonNode> getAppConfigsByGroupName(String groupName) {
        return this.settingFetcher.get(groupName);
    }

    @Override
    public Mono<ServerResponse> getQRCodeImg(ServerRequest serverRequest) {
        String token = tokenManager.getToken();
        String page = "pages/index/index";
        String postId = serverRequest.pathVariable("postId");

        return qrCodeInfoService.fetchByPostId(postId).flatMap(qrCodeInfoHas -> {
            if (Objects.isNull(qrCodeInfoHas.getImageUrl())) {
                return qrCodeInfoService.delete(qrCodeInfoHas).flatMap(deleted -> {
                    return getQRCodeImg(serverRequest);
                });
            } else {
                String imageUrl = externalLinkProcessor.processLink(qrCodeInfoHas.getImageUrl());
                return ServerResponse.ok().bodyValue(imageUrl);
            }
        }).switchIfEmpty(Mono.defer(() -> {

            Map<String, String> valueMap = new HashMap<>();
            valueMap.put("postId", postId);
            QRCodeInfo qrCodeInfo = new QRCodeInfo();
            qrCodeInfo.setPostId(postId);

            return qrCodeInfoService.save(qrCodeInfo).flatMap(info -> {
                if (!Objects.isNull(token)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("scene", info.getKey());
                    map.put("page", page);
                    map.put("width", 200);
                    map.put("auto_color", true);
                    map.put("is_hyaline", false);

                    Mono<byte[]> monoBytes = WEB_CLIENT.post().uri(GET_WXA_CODE_URL + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(JSONObject.toJSONString(map)), String.class)
                            .retrieve()
                            .bodyToMono(byte[].class);

                    return monoBytes.flatMap(bytes -> {
                        return this.getMediaUrl(postId, bytes).flatMap(url -> {
                            info.setImageUrl(url);
                            return qrCodeInfoService.update(info).flatMap(updatedInfo -> {
                                String imageUrl = externalLinkProcessor.processLink(updatedInfo.getImageUrl());
                                return ServerResponse.ok().bodyValue(imageUrl);
                            });
                        });
                    });

                } else {
                    return getAccessToken().flatMap(accessTokenJson -> {
                        String accessToken = accessTokenJson.get("access_token").asText();
                        int expiresIn = accessTokenJson.get("expires_in").asInt();
                        tokenManager.setToken(accessToken, expiresIn);
                        return getQRCodeImg(serverRequest);
                    });
                }
            });
        }));
    }

    @Override
    public Mono<ServerResponse> getQRCodeInfo(ServerRequest serverRequest) {
        String key = serverRequest.pathVariable("key");
        return qrCodeInfoService.fetchByKey(key).flatMap(info -> {
            if (Objects.isNull(info)) {
                return ServerResponse.notFound().build();
            } else {
                return ServerResponse.ok().body(Mono.just(info), QRCodeInfo.class);
            }
        });
    }

    private Mono<JsonNode> getAccessToken() {
        return this.getAppConfigsByGroupName("appConfig").flatMap(config -> {
            JsonNode appInfo = config.get("appInfo");
            String appId = appInfo.get("appId").asText();
            String appSecret = appInfo.get("appSecret").asText();
            String url = TOKEN_URL + "?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
            return WEB_CLIENT.get().uri(url).retrieve().bodyToMono(JsonNode.class);
        });
    }

    private Mono<Attachment> uploadMedia(String postId, byte[] mediaData) {
        FilePart filePart = new SimpleFilePart(postId + "." + MediaType.IMAGE_PNG.getSubtype(), byteArrayToFlux(mediaData), MediaType.IMAGE_PNG);

        return this.getAppConfigsByGroupName("appConfig").flatMap(config -> {
            JsonNode appInfo = config.get("appInfo");
            String policyName = appInfo.get("policyName").asText();
            String fileGroupName = appInfo.get("fileGroupName").asText();

            return attachmentService.upload("admin", policyName, fileGroupName, filePart, null);
        });
    }

    private Flux<DataBuffer> byteArrayToFlux(byte[] bytes) {
        DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();
        return Flux.just(bytes)
                .map(data -> bufferFactory.wrap(ByteBuffer.wrap(data)));
    }

    private Mono<String> getMediaUrl(String postId, byte[] mediaData) {
        return uploadMedia(postId, mediaData).flatMap(attachment -> getPermalink(attachment));
    }

    private Mono<String> getPermalink(Attachment attachment) {
        return attachmentService.getPermalink(attachment).flatMap(uri -> Mono.just(uri.toString()));
    }
}