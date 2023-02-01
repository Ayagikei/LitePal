/*
 * Copyright (C)  Tony Green, Litepal Framework Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litepal.litepalsample.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import org.litepal.crud.LitePalSupport;

@Entity(tableName = "album")
public class Album extends LitePalSupport {

	@PrimaryKey
	private long id;

//    @Column(ignore = false, unique = false, nullable = false, defaultValue = "888")
    private int sales;

//    @Column(nullable = false)
	private String name;

//    @Column(ignore = false, nullable = false)
	private String publisher;

//    @Column(nullable = false, ignore = false)
	private double price;

//    @Column(unique = true, ignore = false)
    private String serial;

//    @Column(ignore = false, nullable = false, defaultValue = "100")
	private long release;

	private Long singer_id;

	@Ignore
	private Singer singer;

	//mprivate List<Song> songs = new ArrayList<Song>();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public long getRelease() {
		return release;
	}

	public void setRelease(long release) {
		this.release = release;
	}

	public Long getSinger_id() {
		return singer_id;
	}

	public void setSinger_id(Long singer_id) {
		this.singer_id = singer_id;
	}

	public Singer getSinger() {
		return singer;
	}

	public void setSinger(Singer singer) {
		this.singer = singer;
	}
	//
	//public List<Song> getSongs() {
	//	return songs;
	//}
	//
	//public void setSongs(List<Song> songs) {
	//	this.songs = songs;
	//}

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public int getSales() {
        return sales;
    }

    public void setSales(int sales) {
        this.sales = sales;
    }

	public Album(long id, int sales, String name, String publisher, double price, String serial, long release,
		Long singer_id) {
		this.id = id;
		this.sales = sales;
		this.name = name;
		this.publisher = publisher;
		this.price = price;
		this.serial = serial;
		this.release = release;
		this.singer_id = singer_id;
	}

	@Ignore
	public Album() {
	}
}
