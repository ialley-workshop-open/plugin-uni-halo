package pro.uhalo.uni.controller;

import pro.uhalo.uni.constants.Constants;
import pro.uhalo.uni.service.UniHaloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.plugin.ApiVersion;

@ApiVersion(Constants.PUBLIC_CUSTOM_API_GROUP_NAME)
@RequestMapping("/uni-halo")
@RestController
@Slf4j
public class UniHaloController {
    private final UniHaloService uniHaloService;

    public UniHaloController(UniHaloService uniHaloService) {
        this.uniHaloService = uniHaloService;
    }

}
