package pro.uhalo.uni.finders.impl;

import pro.uhalo.uni.finders.UniHaloFinder;
import lombok.RequiredArgsConstructor;
import run.halo.app.theme.finders.Finder;
import run.halo.app.extension.ReactiveExtensionClient;
/**
 * 接口实现类
 *
 * @author 小莫唐尼
 */
@Finder("uniHaloFinder")
@RequiredArgsConstructor
public class UniHaloFinderImpl implements UniHaloFinder {

    private final ReactiveExtensionClient client;

}