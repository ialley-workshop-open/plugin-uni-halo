package cn.ialley.unihalo.scheme;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * @author: lywq
 * @date: 2024/08/01 11:27
 * @version: v1.0.0
 * @description:
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "QRCodeInfo", plural = "qRCodeInfos", singular = "qRCodeInfo")
public class QRCodeInfo extends AbstractExtension {

    private String key;
    private String postId;
    private String imageUrl;

}