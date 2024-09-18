package com.jfirer.jfire.test.function.importtest;

import com.jfirer.jfire.core.prepare.annotation.Import;

import javax.annotation.Resource;

@Resource
@Import(Node2.class)
public class Node1
{
}
