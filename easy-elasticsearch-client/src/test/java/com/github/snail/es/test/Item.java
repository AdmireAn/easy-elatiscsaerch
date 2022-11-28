package com.github.snail.es.test;

import com.github.snail.annotation.Doc;
import com.github.snail.annotation.Id;
import com.github.snail.annotation.PartitionFactor;

/**
 * @author wangyongan <wangyongan@kuaishou.com>
 * Created on 2022-11-26
 */
@Doc(sourceKey = "easy-write-elasticsearch")
public class Item {
    @Id
    @PartitionFactor
    private long itemId;
    private String itemName;
    private long price;

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}
