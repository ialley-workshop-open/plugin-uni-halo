package pro.uhalo.uni.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.uhalo.uni.scheme.QRCodeInfo;
import pro.uhalo.uni.services.QRCodeInfoService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;

import java.security.SecureRandom;
import java.time.Instant;
import org.springframework.data.domain.Sort;

import static run.halo.app.extension.index.query.QueryFactory.equal;

/**
 * @author: lywq
 * @date: 2024/08/01 15:29
 * @version: v1.0.0
 * @description:
 **/
@Service
@RequiredArgsConstructor
public class QRCodeInfoServiceImpl implements QRCodeInfoService {

    private final ReactiveExtensionClient client;

    @Override
    public Mono<QRCodeInfo> fetchByKey(String key) {
        return client.fetch(QRCodeInfo.class, key);
    }

    @Override
    public Mono<QRCodeInfo> fetchByPostId(String postId) {
        var listOptions = new ListOptions();
        var query = equal("postId", postId);
        listOptions.setFieldSelector(FieldSelector.of(query));
        Flux<QRCodeInfo> friendFlux = client.listAll(QRCodeInfo.class, listOptions, Sort.unsorted());
        return friendFlux.next();
    }

    @Override
    public Mono<QRCodeInfo> save(QRCodeInfo qrCodeInfo) {
        String key = generateRandomString(16);
        qrCodeInfo.setKey(key);
        return this.fetchByKey(key)
                .flatMap(info -> this.save(qrCodeInfo))
                .switchIfEmpty(Mono.defer(() -> {
                    Metadata metadata = new Metadata();
                    metadata.setName(key);
                    metadata.setCreationTimestamp(Instant.now());
                    qrCodeInfo.setMetadata(metadata);
                    return client.create(qrCodeInfo);
                }));
    }

    @Override
    public Mono<QRCodeInfo> update(QRCodeInfo qrCodeInfo) {
        return client.update(qrCodeInfo);
    }

    @Override
    public Mono<QRCodeInfo> delete(QRCodeInfo qrCodeInfo) {
        return client.delete(qrCodeInfo);
    }

    private static String generateRandomString(int stringLength) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(stringLength);
        for (int i = 0; i < stringLength; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}
