/*
 *  Copyright 2019-2025 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.modules.system.service.impl;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.vo.EmailVo;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.modules.system.service.VerifyService;
import me.zhengjie.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author Zheng Jie
 * @date 2018-12-26
 */
@Service
@RequiredArgsConstructor
public class VerifyServiceImpl implements VerifyService {

    private final RedisUtils redisUtils;
    @Value("${code.expiration}")
    private Long expiration;

    @Override
    public EmailVo sendEmail(String email, String key) {
        EmailVo emailVo;
        String content;
        String redisKey = key + email;
        TemplateEngine engine = TemplateUtil.createEngine(new TemplateConfig("template", TemplateConfig.ResourceMode.CLASSPATH));
        Template template = engine.getTemplate("email.ftl");
        String oldCode = redisUtils.get(redisKey, String.class);
        if (oldCode == null) {
            String code = RandomUtil.randomNumbers(6);
            if (!redisUtils.set(redisKey, code, expiration)) {
                throw new BadRequestException("Server error");
            }
            content = template.render(Dict.create().set("code", code));
        } else {
            content = template.render(Dict.create().set("code", oldCode));
        }
        emailVo = new EmailVo(Collections.singletonList(email), "Sport Revive Rating Registration", content);
        return emailVo;
    }

    @Override
    public void validated(String key, String code) {
        String value = redisUtils.get(key, String.class);
        if (!code.equals(value)) {
            throw new BadRequestException("Code invalid");
        } else {
            redisUtils.del(key);
        }
    }
}
