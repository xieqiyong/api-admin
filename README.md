

### jmeter压测命令合集
1.  jmeter -n -t /Users/liusu/我的测试脚本.jmx

### 接下来的规划
1. 压测引擎打包
2. 分布式发压，包括csvdata

### 压测引擎
1. ./gradlew build
2. ./gradlew createDist
3. ./gradlew runGui