# trec-preprocess

对 TREC CDS 的数据集进行预处理，将 nxml 格式摘取部分内容，去除 XML 格式化信息，生成 json 供使用。

### 构建

```sh
$ mvn package
```

文件 `/target/ir-trec-preprocess-*.jar` 即为可独立部署使用的 jar 包。

### 运行

```sh
$ jar -jar trec-preprocess.jar <threadNum> <sourceRoot> <targetDir>
```

第一个参数是指定使用的线程数，第二个参数是源文件目录，第三个参数是目标文件目录。

程序将递归地扫描 `sourceRoot` 目录下的 `*.nxml` 文件，处理后输出到 `targetDir/*.nxml.json`，注意输出时不保留原目录结构。

如果 `.json` 文件已存在且非空，那么将跳过，不作处理。

### 采用的技术和库

- nio
- 多线程
- SAX (for xml)
- Jackson (for json)

### 修改

扫描输入和统计的部分在 `AppMain` 文件里，对格式的处理主要在 `NxmlHandler#endElement` 方法里，`Result` 是输出的对象。

### 其他

祝君水课愉快，欢迎 star/follow。
