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
package me.zhengjie.modules.quartz.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * For testing
 * @author Zheng Jie
 * @date 2019-01-08
 */
@Slf4j
@Service
public class TestTask {

    public void run(){
        log.info("run executed successfully");
    }

    public void run1(String str){
        log.info("run1 executed successfully, parameter is: {}", str);
    }

    public void run2(){
        log.info("run2 executed successfully");
    }
}
