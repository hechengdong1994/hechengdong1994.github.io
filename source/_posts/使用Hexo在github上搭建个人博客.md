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

3）博客逻辑上分类使用categories方式组织，物理上使用文件夹划分，即文章根据分类来划分文件夹。对于博客来说，关键词才是体现内容的方式，分类只是一个粗略大类的划分。虽然技术上可以通过md中的Front-matter部分指定多个分类，但原则上不进行指定，一篇博客仅属于一个分类。

1）官方有一个hexo new post的命令可以使用模板生成md文件，但是由于我自己不想依赖hexo的功能，只是想用它来做系统搭建。因此，对于博客文档和资源文件夹的创建，我还是使用手工的方式来做。

使用官方的hello-world.md示例测试图片资源的引用。创建hello-world文件夹并放入一张图片，在hello-world.md中引用该图片，启动后图片无法展示。查了一下，参考https://hexo.io/zh-cn/docs/asset-folders 官方文档。开启post_asset_folder配置项

```yml
post_asset_folder: true
```

重新生成后可用。

注：如果仅修改post_asset_folder配置，不修改permalink配置，则网页中的图片是无法正常解析的。可以从hexo g命令的结果中看到，图片文件被保存为“/日期/博客标题/图片文件名”，而静态页中的图片引用为“/博客标题/图片文件名”，因此解析不到。

2）

在博客中，使用Front-matter指定tags，能够在博客详情页中看到对应的展示。

但是官方提供的例子中是没有展示主题页的，这个需要所使用的主题有支持才会展示。在原始主题下，如果手动打开页面会显示Cannot GET /tags/。这是因为这个页面本身不存在，因此首先需要生成这个页面，再由主题去解析展示。

通过hexo new page tags。则source文件夹下会出现tags文件夹，其中包含一个index.md文件。修改该md文件，在Front-matter中增加

```yml
type: "tags"
layout: "tags"
```

由于不同的主题对这个页面的处理的方式不同，有的是根据type，有的是根据layout，有的直接根据文件夹名称，所以我这边都加上了。

同样tags的统计和词云的展示，则是由主题提供的。

3）与tags的处理方式相同。

不同点在于categories有对应的文件夹。



主题选择

更改主题，并按需配置调整



部署到github.io

配置github page

https://hexo.io/zh-cn/docs/github-pages#%E7%A7%81%E6%9C%89-Repository

https://hexo.io/zh-cn/docs/one-command-deployment

修改deploy配置项并部署，访问github.io，与本地访问结果相同。

使用github actions支持静态网页自动部署

### 添加推送用的key

在本地用git bash生成一堆新的密钥

1. `ssh-keygen -f HEXO_DEPLOY_KEY -C "HEXO_DEPLOY_KEY"`

在HEXO部署仓库，来到settings，打开Deploy keys，添加一个新的key，上传公钥，即刚生成的pub文件内容，名字为HEXO_DEPLOY_KEY_PUB

在博客源码仓库，settings，secrets，actions，将私钥复制粘贴到这里面，名字为HEXO_DEPLOY_KEY_PRI，**记住这个secret的名字**

### 配置Action

这里可以在github上点击 `Action -> new workflow -> set up a workflow yourself`随便挑个模板将内容放进去，也可以在代码仓库中的`.github/workflow`添加一个`hexo-ci.yml`然后将以下内容放进该文件中。

**记得把<>中的 `blog_source_branch`, `username`, `username@email.address`替换成你自己的**

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
    strategy:
      matrix:
        node-version: [14.x]
    steps:
      - name: Checkout
      uses: actions/checkout@v2
      with:
        submodules: true # Checkout private submodules(themes or something else).

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

这样就会在每次push的时候触发这个Action，自己生成并部署hexo了~

## 配置Hexo deploy

打开代码仓库中的`_config.yml`

将 git 存储库从 http 形式更改为 ssh 形式。

发布部署文件的的分支应该仓库配置的分支一样

```yml
deploy:
  type: git
  repository: git@github.com:xxx/xxx.github.io.git
  # example, https://github.com/hexojs/hexojs.github.io
  branch: gh-pages
```

你已经完成了所有操作！推送一次看看效果吧。

问题：

博客标题中不能出现html的url符号，在生成静态网页时会不正确



https://www.githang.com/2018/12/22/hexo-new-post-path/

https://github.com/iTimeTraveler/hexo-theme-hiker