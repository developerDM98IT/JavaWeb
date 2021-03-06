package com.laptrinhjavaweb.dao.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.laptrinhjavaweb.dao.INewsDao;
import com.laptrinhjavaweb.mapper.NewMapper;
import com.laptrinhjavaweb.model.NewsModel;
import com.laptrinhjavaweb.paging.Pageble;

public class NewsDao extends AbtractDao<NewsModel> implements INewsDao {

	@Override
	public List<NewsModel> findByCategoryId(Long id) {
		String sql = "select * from news where category_id = ?";
		return query(sql, new NewMapper(), id);
	}

	@Override
	public Long Insert(NewsModel entity) {
		StringBuffer sql = new StringBuffer("INSERT INTO `news`(`category_id`, `title`, `thumbnail`,");
		sql.append("`shortdecription`, `content`, `createddate`, `createdby`)");
		sql.append("VALUES(?,?,?,?,?,?,?)");
		Object[] parameter = new Object[] {
				entity.getCategory_id(),
				entity.getTitle(),
				entity.getThumbnail(),
				entity.getShortdecription(),
				entity.getContent(),
				entity.getCreateddate(),
				//entity.getModifieddate(),
				entity.getCreatedby()
				//entity.getModifiedby()
		};
		
		return Insert(sql.toString(), parameter);
		
	}

	@Override
	public NewsModel findOne(Long id) {
		String sql = "select * from news where id = ?";
		List<NewsModel> dataNews = query(sql, new NewMapper(), id);
		return dataNews.isEmpty() ? null : dataNews.get(0);
	}

	@Override
	public void update(NewsModel entity) {
		String sql = " UPDATE news SET "
				+ "category_id=?, "
				+ "title= ?, "
				+ "thumbnail= ?,"
				+ "shortdecription= ?,"
				+ "content= ? ,"
				+"createddate = ? ," 
				+"modifieddate = ? ," 
				+"createdby = ? ," 
				+"modifiedby = ?"
				+ "WHERE id= ?";
		Object[] parameter = new Object[] {
				entity.getCategory_id(),
				entity.getTitle(),
				entity.getThumbnail(),
				entity.getShortdecription(),
				entity.getContent(),
				entity.getCreateddate(),
				entity.getModifieddate(),
				entity.getCreatedby(),
				entity.getModifiedby(),
				entity.getId()
		};
		Update(sql, parameter);
	}

	@Override
	public void delete(Long ids) {
		String sql = "DELETE FROM `news` WHERE  id =?";
		Update(sql, ids);
	}

	

	@Override
	public int getTotalItem() {
		String sql = "select count(*) from news";
		return count(sql);
	}

	@Override
	public List<NewsModel> FindAll(Pageble pageble) {
			StringBuilder sql = new StringBuilder("select * from news");
			if(pageble.getSorter() != null && StringUtils.isNotBlank(pageble.getSorter().getSortName()) && StringUtils.isNotBlank(pageble.getSorter().getSortBy())) {
				sql.append(" order by "+pageble.getSorter().getSortName()+" "+pageble.getSorter().getSortBy()+" ");
			}
			if(pageble.getOffset() != null && pageble.getLimit() != null ) {
				sql.append("limit "+pageble.getOffset()+","+pageble.getLimit()+" ");
			}
			return query(sql.toString(), new NewMapper());
	}

}
