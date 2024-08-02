package pro.uhalo.uni.services;

import pro.uhalo.uni.scheme.QRCodeInfo;
import reactor.core.publisher.Mono;

/**
 * @author: lywq
 * @date: 2024/08/01 15:25
 * @version: v1.0.0
 * @description:
 **/
public interface QRCodeInfoService {

    Mono<QRCodeInfo> fetchByKey(String key);

    Mono<QRCodeInfo> fetchByPostId(String postId);

    Mono<QRCodeInfo> save(QRCodeInfo qrCodeInfo);

    Mono<QRCodeInfo> update(QRCodeInfo qrCodeInfo);

    Mono<QRCodeInfo> delete(QRCodeInfo qrCodeInfo);
}
