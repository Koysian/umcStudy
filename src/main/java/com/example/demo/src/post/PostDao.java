package com.example.demo.src.post;

import com.example.demo.src.post.model.*;
import com.example.demo.src.user.model.GetUserPostsRes;
import com.example.demo.src.user.model.GetUserRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class PostDao {

    private JdbcTemplate jdbcTemplate;
    private List<GetPostImgRes> getPostImgRes;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }



    public List<GetPostsRes> selectPosts(int userIdx){
        String selectPostsQuery = "SELECT p.postIdx as postIdx, u.userIdx as userIdx, u.nickName as nickName, u.profileImgUrl as profileImgUrl, p.content as content,\n" +
                "       IF(spl.postLikeCount is null, 0, spl.postLikeCount) as postLikeCount,\n" +
                "       IF(commentCount is null, 0, commentCount) as commentCount\n" +
                "FROM Post as p\n" +
                "    join User as u on u.userIdx = p.userIdx\n" +
                "    left join (select postIdx, userIdx, count(postLikeIdx) as postLikeCount from PostLike WHERE status = 'ACTIVE' group by postIdx) spl on spl.postIdx = p.postIdx\n" +
                "    left join (select postIdx, count(commentIdx) as commentCount from Comment WHERE status = 'ACTIVE') c on c.postIdx = p.postIdx\n" +
                "WHERE p.status = 'ACTIVE' and u.userIdx = ?\n" +
                "group by p.postIdx;";
        int selectPostsParam = userIdx;


        return this.jdbcTemplate.query(selectPostsQuery, (rs, rowNum) -> new GetPostsRes(
                        rs.getInt("postIdx"),
                        rs.getInt("userIdx"),
                        rs.getString("nickName"),
                        rs.getString("profileImgUrl"),
                        rs.getString("content"),
                        rs.getInt("postLikeCount"),
                        rs.getInt("commentCount"),
                        getPostImgRes=this.jdbcTemplate.query("SELECT  pi.postUrlIdx, pi.imgUrl\n" +
                                "From PostImgUrl as pi\n" +
                                "    join Post as p on p.postIdx = pi.postIdx\n" +
                                "WHERE pi.status = 'ACTIVE' and p.postIdx = ?;", (rk, rownum) -> new GetPostImgRes(
                                        rk.getInt("postUrlIdx"),
                                        rk.getString("imgUrl")
                        ), rs.getInt("postIdx"))), selectPostsParam);
    }

    public int checkUserExist(int userIdx){
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx=?);";
        int checkUserExistParam = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery, int.class, checkUserExistParam);
    }

    public int insertPosts(int userIdx, String content){
        String insertPostsQuery = "INSERT INTO Post(userIdx, content) VALUES (?, ?)";
        Object[] insertPostParams = new Object[] {userIdx, content};
        this.jdbcTemplate.update(insertPostsQuery,insertPostParams);

        String lastPostIdx = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastPostIdx, int.class);
    }

    public void insertPostImgs(int postIdx, PostImgUrlReq postImgUrlReq){
        String insertPostImgQuery = "INSERT INTO PostImgUrl(postIdx, imgUrl) VALUES (?, ?)";
        Object[] insertPostParams = new Object[] {postIdx, postImgUrlReq.getImgUrl()};
        this.jdbcTemplate.update(insertPostImgQuery,insertPostParams);
    }

    public int updatePost(int postIdx, String content){
        String updatePostQuery = "UPDATE Post SET content=? WHERE postIdx=?;";
        Object[] updatePostParams = new Object[] {content, postIdx};
        this.jdbcTemplate.update(updatePostQuery, updatePostParams);

        String lastPostIdx = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastPostIdx, int.class);
    }

    public int checkPostExist(int postIdx){
        String checkPostExistQuery = "select exists(select postIdx from Post where postIdx=?);";
        int checkPostExistParam = postIdx;
        return this.jdbcTemplate.queryForObject(checkPostExistQuery, int.class, checkPostExistParam);
    }

    public void deletePost(int postIdx){
        String deletePostQuery = "UPDATE Post SET status='INACTIVE' WHERE postIdx=?;";
        Object[] deletePostParam = new Object[] {postIdx};
        this.jdbcTemplate.update(deletePostQuery, deletePostParam);
    }
    public String getPostStatus(int postIdx){
        String getPostStatusQuery = "SELECT status FROM Post WHERE postIdx=?;";
        Object[] getPostStatusParam = new Object[] {postIdx};

        return this.jdbcTemplate.queryForObject(getPostStatusQuery, String.class, getPostStatusParam);
    }

}
