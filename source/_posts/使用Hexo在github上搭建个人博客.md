---
title: 使用hexo在github上搭建个人博客
date: 2022-08-10
tags: 
  - github
  - hexo
  - blog
---

# 摘要

本文以工程实践的方式，按照需求提出、方案选择、开发实施、测试上线的流程，描述了为什么会有搭建博客系统的需求、如何使用hexo框架搭建博客系统并自动构建托管到github.io上的完整过程。特别地，对于开发实施部分，是根据实操的情况，采用不断地提出需求和问题并解决的方式来描述的。

<!-- more -->

# 1 需求提出

费曼学习法作为一个被广泛认可的学习方式，其中提出的输出步骤，可以说是整个学习法的最重要的部分。相比于阅读、听课等输入，输出有时被认为是一种更高效的学习方式：输出能够帮助加深印象，并帮助了解自己在哪个部分有所欠缺。

文章/博客作为一种系统性的知识载体，可以认为是一种很好的输出方式。其中，博客又以其行文格式开放的特点被大家广泛使用。

在技术门槛和成本逐渐降低的今天，自己手动搭建一个由自己定制化的博客也不失为一种让人心动的方式。所以，拥有一个属于自己的个人博客，也成为一个自然而然的需求。

当然，由于博客仅是一个平台，因此不应在搭建上花费过多的精力。

所以，最终的完整需求是**低成本地搭建个人博客网站**。

# 2 方案

开源的、简单的方案总是更容易受到大家的青睐。查了一些资料后，最终选择了比较广泛使用的hexo搭建博客+github.io托管的方案。

hexo是一个基于node.js的、快速、简洁且高效的博客框架。支持使用markdown解析文章，并可以快速生成静态网页，同时支持主题的定制。

github pages是github为个人/组织、项目提供的一个网站。只需编辑并推送，对应的改动就会在网页上生效。而其中的github.io是github pages为个人提供的免费网站。

# 3 软件安装

必选的需要安装node.js、hexo、git。另外还安装github的图形界面客户端github desktop。

node.js：直接在官网 http://nodejs.org/ 下载安装包运行安装即可。

hexo：参考官方中文文档进行安装： https://hexo.io/zh-cn/docs/ 。

# 4 建站

hexo由于其简单易用的特点，其搭建过程也十分容易，在官方文档中有详细的描述，因此主要是参考官方文档来进行。

## 4.1 初始化

参考 https://hexo.io/zh-cn/docs/setup 和 https://hexo.io/zh-cn/docs/commands 部分。

1. 执行初始化


```shell
$ hexo init
INFO  Cloning hexo-starter https://github.com/hexojs/hexo-starter.git
INFO  Install dependencies
npm WARN config global `--global`, `--local` are deprecated. Use `--location=global` instead.
INFO  Start blogging with Hexo!
```

出现最后一句【INFO  Start blogging with Hexo!】表示初始化成功。

*注：初始化需要在一个空文件夹中执行。上传到github.io仓库前需要将生成的文件内容复制到对应的本地仓库中。*

2. 解析依赖

初始化结果中的node_modules文件夹为外部依赖包（类比lib）。官方文档中，在初始化后，又对依赖显式地进行了重新解析，因此实际操作中也删除该文件夹并重新执行依赖解析。

```shell
$ npm install
npm WARN config global `--global`, `--local` are deprecated. Use `--location=global` instead.
npm WARN deprecated source-map-resolve@0.6.0: See https://github.com/lydell/source-map-resolve#deprecated

added 240 packages, and audited 241 packages in 5s

19 packages are looking for funding
  run `npm fund` for details

found 0 vulnerabilities
```

3. 本地启动


```shell
$ hexo server
INFO  Validating config
INFO  Start processing
INFO  Hexo is running at http://localhost:4000/ . Press Ctrl+C to stop.
```

使用浏览器测试可以正常访问。

## 4.2 支持静态网页

参考 https://hexo.io/zh-cn/docs/commands#generate ，生成静态网页并启动，访问正常。

在测试过程中发现默认生成的静态网页的地址是“/日期/博客标题”的格式，根据 https://hexo.io/zh-cn/docs/permalinks 的说明，生成的静态网页的默认地址格式为“/博客创建日期/博客路径/博客标题”。如果其中的创建日期和博客路径发生变化，那么相应的外部引用就失效了，因此希望地址中采用相对稳定不会变化的内容来标识。根据文档提供的可选值，最终决定选择“/文章标题”作为静态地址格式。

根据文档说明，调整配置。重新生成静态网页后测试，功能符合预期。

```yml
permalink: :post_title/
```

## 4.3 常用功能

### 4.3.1 资源引用

出于资源管理的方便性考虑，希望将博客中引用的资源文件放在与文章同级的同名的文件夹中。由于官方提供的md文档中没有使用到图片等外部资源，因此需要进行可行性测试。

使用官方的hello-world.md示例测试图片资源的引用。创建hello-world文件夹并放入一张图片，在hello-world.md中使用md语法引用该图片，启动后图片无法展示。

参考官方文档 https://hexo.io/zh-cn/docs/asset-folders ，开启post_asset_folder配置。重新生成后测试可达成需求。

```yml
post_asset_folder: true
```

*注：如果仅修改post_asset_folder配置，不修改permalink配置，则网页中的图片是无法正常解析的。可以从hexo g命令的输出结果中看到，图片文件被保存为“/日期/博客标题/图片文件名”，而静态页中的图片引用为“/博客标题/图片文件名”，因此解析不到。*

### 4.3.2 分类/标签/文件目录的规划

需求描述：

1. 主题/关键词用于标识一篇博客的内容主要跟哪些方面有关，一篇博客可以有多个tag。
2. 对于博客来说，关键词更适合作为体现内容的方式，分类只是逻辑上的一个粗略大类的划分。博客在物理上可以根据分来来进行文件夹划分。因此一篇博客仅属于一个分类。
3. 博客目录按分类进行目录划分。

解决方案：

在博客的Front-matter部分指定tags/categories，来标识所属的分类和标签主题。

## 4.4 主题选择

根据 https://hexo.io/zh-cn/docs/themes 进行主题更改。一般来说，各主题都会提供详细的官方文档。

我选择的是fluid主题： https://github.com/fluid-dev/hexo-theme-fluid 。

一般主题内都有一些功能可选，如评论系统，阅读量统计等。实际选择valine作为评论系统和leadcloud平台进行访问统计。

## 4.5 分类/标签展示页缺失

主题一般都是支持分类页和标签页的。在替换主题后，打开页面会显示**Cannot GET /categories/**和**Cannot GET /tags/**。这是因为页面本身不存在，因此首先需要生成这个页面，再由主题去解析展示。

根据 https://hexo.io/zh-cn/docs/commands#new 内容，通过hexo new page tags在source文件夹下生成tags文件夹，其中包含一个index.md文件。修改该md文件，在Front-matter中增加

```yml
type: "tags"
layout: "tags"
```

由于不同的主题对这个页面的处理的方式不同，有的是根据type，有的是根据layout，有的直接根据文件夹名称，所以这边都加上了。

# 5 部署到github.io

## 5.1 手动配置部署

参考 https://hexo.io/zh-cn/docs/github-pages#%E7%A7%81%E6%9C%89-Repository 和 https://hexo.io/zh-cn/docs/one-command-deployment 。修改deploy配置项并进行部署推送，访问github.io对应地址，与本地访问结果相同。

## 5.2 使用github actions生成并自动部署静态网页

1. 修改hexo的deploy配置，将git存储库从http形式更改为ssh形式。

```yml
deploy:
  type: git
  repository: git@github.com:xxx/xxx.github.io.git
  # example, https://github.com/hexojs/hexojs.github.io
  branch: gh-pages
```

2. 在本地用git bash生成一对新的密钥

```shell
ssh-keygen -f HEXO_DEPLOY_KEY -C "HEXO_DEPLOY_KEY"
```

3. 在部署静态网页的仓库，即github.io仓库，打开settings，打开Deploy keys，添加一个新的key，上传公钥，即刚生成的pub文件内容，名字为HEXO_DEPLOY_KEY_PUB，需要勾选开启写权限。


4. 在源码仓库，此处也是github.io仓库，settings，secrets，actions，添加私钥，名字为HEXO_DEPLOY_KEY_PRI。

5. 配置actions

   在源码仓库上进入 `Action -> new workflow`，选择`set up a workflow yourself`，随便挑个模板；也可以在代码仓库中的`.github/workflow`添加一个`hexo-ci.yml`然后将以下内容放进该文件中。
   
   **记得把<>中的 `blog_source_branch`, `username`, `username@email.address`替换成自己的实际值**

```yaml
name: HEXO CI

on:
  push:
    branches:
    - <blog_source_branch>
    
jobs:
  build:
    name: A job to deploy blog.
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v2
        with:
          # Checkout private submodules(themes or something else).
          submodules: true 

      # Caching dependencies to speed up workflows. (GitHub will remove any cache entries that have not been accessed in over 7 days.)
      - name: Setup Node.js
        uses: actions/setup-node@v1
        with:
          node-version: '12' #超过12的版本有几个warning，我不想处理
      - name: Configuration environment
        env:
          # 定义了一个变量，对应secrets-actions里面配置的私钥的名字
          HEXO_DEPLOY_KEY_PRI: ${{secrets.HEXO_DEPLOY_KEY_PRI}}
        run: |
          # 创建了一个ssh文件夹
          mkdir -p ~/.ssh/
          # 把key复制进去
          echo "$HEXO_DEPLOY_KEY_PRI" > ~/.ssh/id_rsa
          chmod 700 ~/.ssh
          chmod 600 ~/.ssh/id_rsa
          ssh-keyscan github.com >> ~/.ssh/known_hosts
          # 配置git
          git config --global user.name "<username>"
          git config --global user.email "<username@email.address>"
      - name: Install dependencies
        run: |
          # 安装hexo
          npm i -g hexo-cli
          npm i
      - name: Deploy
        run: |
          hexo clean && hexo generate && hexo deploy
```

配置后，就会在每次push到对应分支的时候触发这个Action，自己生成并部署hexo了。

# 6 已知问题

1. 博客标题中不能出现html的url实体符号，生成的静态网页路径会不正确。

