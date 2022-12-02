## 使用
写es:
```java
public void writeEs() {
        Item item = new Item();
        item.setItemId(882301L);
        item.setItemName("大苹果");
        item.setPrice(994404L);
        ArrayList<Item> items = Lists.newArrayList(item);
        esSearchService.bulkUpsert(items);
    }
```
![image-20221202175722161](https://cdn.jsdelivr.net/gh/AdmireAn/blobImage@main/img/image-20221202175722161.png)

读es:
```java
BoolQueryBuilder queryBuilder = BoolQueryBuilder().filter(QueryBuilders.matchAllQuery());
        
        ElasticSearchRestQuery query = new ElasticSearchRestQuery()
                .setQueryBuilder(queryBuilder)
                .setFrom(0)
                .setSize(100);

        SearchResult<Item> result = esSearchService.read(query, SourceKey.EASY_WRITE_ELASTICSEARCH);
        long total = result.total();  //返回符合搜索条件的全部结果条数
        List<Item> itemSearchDocuments = result.entities()
                .stream()
                .map(hit -> hit.source(Item.class))
                .collect(Collectors.toList());
        System.out.println(total);
        System.out.println(ObjectMapperUtils.toJSON(itemSearchDocuments));
```
输出
```shell
4
[{"itemId":109782300,"itemName":"胡萝卜","price":3822404},{"itemId":882301,"itemName":"大苹果","price":994404},{"itemId":9282301,"itemName":"西红柿","price":444404},{"itemId":212301,"itemName":"火龙果","price":664404}]
```

## 如何本地运行

### 安装ES

下载：https://www.elastic.co/cn/downloads/elasticsearch

运行：

```shell
bin/elasticsearch
```
访问：http://localhost:9200

<img src="https://cdn.jsdelivr.net/gh/AdmireAn/blobImage@main/img/image-20221202182611553.png" alt="image-20221202182611553" style="zoom:50%;" />

### 安装Cerebro

下载：

```shell
wget -c https://github.com/lmenezes/cerebro/releases/download/v0.9.3/cerebro-0.9.3.tgz
```

解压：

```shell
tar xfvz cerebro-0.9.3.tgz
```

运行：

```shell
tar xfvz cerebro-0.9.3.tgz
```

访问：127.0.0.0:9000

连接本地es：http://localhost:9200

![image-20221202182125709](https://cdn.jsdelivr.net/gh/AdmireAn/blobImage@main/img/image-20221202182125709.png)


### 创建索引

```shell
{
    "mappings": {
      "easyWriteElasticsearch": {
        "properties": {
          "itemId": {
            "type": "long"
          },
          "itemName": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "price": {
            "type": "long"
          }
        }
      }
    },
    "settings":{
        "index":{
            "number_of_shards":"2",
            "number_of_replicas":"2",
            "search":{
                "slowlog":{
                    "threshold":{
                        "fetch":{
                            "warn":"1s",
                            "info":"800ms"
                        },
                        "query":{
                            "warn":"2s",
                            "info":"1s"
                        }
                    }
                }
            },
            "routing":{
                "allocation":{
                    "total_shards_per_node":3
                }
            },
            "refresh_interval":"1s",
            "indexing":{
                "slowlog":{
                    "threshold":{
                        "index":{
                            "warn":"10s",
                            "info":"5s"
                        }
                    }
                }
            }
        }
    },
    "aliases":{
        "easy-write-elasticsearch-index-alias":{

        }
    }
}
```

用cerebro创建两个索引easy-write-elasticsearch-index-0、easy-write-elasticsearch-index-1.

![image-20221202181039466](https://cdn.jsdelivr.net/gh/AdmireAn/blobImage@main/img/image-20221202181039466.png)

### 运行Test

test目录：com.github.snail.es.test.UsageDemo