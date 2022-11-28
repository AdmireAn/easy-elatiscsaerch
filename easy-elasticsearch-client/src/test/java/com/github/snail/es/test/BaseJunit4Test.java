package com.github.snail.es.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author wangyongan <wangyongan@kuaishou.com>
 * Created on 2022-11-28
 */
@RunWith(SpringJUnit4ClassRunner.class) //使用junit4进行测试
@ContextConfiguration(locations={"classpath:spring.xml"}) //加载配置文件
public class BaseJunit4Test {
}