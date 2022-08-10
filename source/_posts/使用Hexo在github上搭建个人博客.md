---
title: 使用Hexo在github上搭建个人博客
date: 2022-08-10
tags:
- github
- Hexo
- blog
---



需求：低成本搭建个人博客网站

方案：选择了比较普遍使用的github.io+Hexo的方案

Hexo：

介绍：Hexo 是一个基于Node.js的、快速、简洁且高效的博客框架。支持使用Markdown解析文章，并可以快速生成静态网页，同时支持主题的定制。

安装：

参考Hexo的官方中文文档：https://hexo.io/zh-cn/docs/

[Node.js](http://nodejs.org/)：直接在官网下载安装包运行安装即可

建站：

初始化

主要参考https://hexo.io/zh-cn/docs/setup和https://hexo.io/zh-cn/docs/commands

执行初始化

```shell
$ hexo init
INFO  Cloning hexo-starter https://github.com/hexojs/hexo-starter.git
INFO  Install dependencies
npm WARN config global `--global`, `--local` are deprecated. Use `--location=global` instead.
INFO  Start blogging with Hexo!
```

出现最后一句【INFO  Start blogging with Hexo!】表示初始化成功。

该命令需要在一个空文件夹中执行。初始化后将生成的文件复制到本地的github.io仓库中。

切换目录到本地的github.io仓库。

文件夹node_modules为外部依赖包（类比lib），由于官方文档显式进行了依赖解析，因此删除该文件夹并重新执行依赖解析。

```shell
$ npm install
npm WARN config global `--global`, `--local` are deprecated. Use `--location=global` instead.
npm WARN deprecated source-map-resolve@0.6.0: See https://github.com/lydell/source-map-resolve#deprecated

added 240 packages, and audited 241 packages in 5s

19 packages are looking for funding
  run `npm fund` for details

found 0 vulnerabilities
```

本地启动

```shell
$ hexo server
INFO  Validating config
INFO  Start processing
INFO  Hexo is running at http://localhost:4000/ . Press Ctrl+C to stop.
```

到此就搭建完成了官方提供的最原始的博客。

静态网页：

使用hexo g生成静态网页并启动，访问正常。

但发现Hexo生成的博客的静态网页的地址是“/日期/博客标题”的格式。

根据https://hexo.io/zh-cn/docs/permalinks了解到默认格式为“/日期/博客路径/博客标题”。但是我不太喜欢，因为这里面日期和博客路径都有可能变化，那外部引用就失效了。希望采用“/文章标题”的方式作为每篇博客的静态地址。

所以调整配置

```yml
permalink: :post_title/
```

重新生成后可用。

对博客常用功能的扩展：

1.资源引用和目录规划：官方提供的md文档中没有使用到图片等外部资源，因此需要进行一些测试。同时，也要对博客文件和资源文件的存放方式进行一定的规划。

1）博客中的其他资源文件放在与文章同级的同名的文件夹中

2）文章主题/关键词使用tags方式进行标记，一篇博客可以有多个tag

3）文章分类使用categories方式组织，物理上使用文件夹划分，即文章根据分类来划分文件夹。对于博客来说，关键词才是体现内容的方式，分类只是一个粗略大类的划分。虽然技术上可以通过md中的Front-matter部分指定多个分类，但原则上不进行指定，一篇博客仅属于一个分类。

1）官方有一个hexo new post的命令可以使用模板生成md文件，但是由于我自己不想依赖hexo的功能，只是想用它来做系统搭建。因此，对于博客文档和资源文件夹的创建，我还是使用手工的方式来做。

使用官方的hello-world.md示例测试图片资源的引用。创建hello-world文件夹并放入一张图片，在hello-world.md中引用该图片，启动后图片无法展示。查了一下，参考https://hexo.io/zh-cn/docs/asset-folders 官方文档。开启post_asset_folder配置项

```yml
post_asset_folder: true
```

重新生成后可用。

注：如果仅修改post_asset_folder配置，不修改permalink配置，则网页中的图片是无法正常解析的。可以从hexo g命令的结果中看到，图片文件被保存为“/日期/博客标题/图片文件名”，而静态页中的图片引用为“/博客标题/图片文件名”，因此解析不到。





问题：

博客标题中不能出现html的url符号，在生成静态网页时会不正确