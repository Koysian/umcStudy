package com.example.demo.src.user;


import com.example.demo.src.user.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public GetUserInfoRes selectUserInfo(int userIdx){
        String selectUsersInfoQuery = "SELECT  u.nickName as nickName, u.name as name, u.profileImgUrl as profileImgUrl, u.website as website,\n" +
                "        u.introduction as introduction,\n" +
                "        IF(postCount is null, 0, postCount) as postCount,\n" +
                "        IF(f1.followerCount is null, 0, f1.followerCount) as followCount,\n" +
                "        IF(f2.followingCount is null, 0, f2.followingCount) as followingCount\n" +
                "FROM User as u\n" +
                "    left join (select userIdx, count(postIdx) as postCount from Post where status = 'ACTIVE' group by userIdx) p on p.userIdx = u.userIdx\n" +
                "    left join (select followerIdx, count(followIdx) as followerCount from Follow where status = 'ACTIVE' group by followerIdx) f1 on f1.followerIdx = u.userIdx\n" +
                "    left join (select followeeIdx, count(followIdx) as followingcount from Follow where status = 'ACTIVE' group by followeeIdx) f2 on f2.followeeIdx = u.userIdx\n" +
                "WHERE u.userIdx = ?;";

        int selectUserInfoParam = userIdx;

        return this.jdbcTemplate.queryForObject(selectUsersInfoQuery, (rs, rowNum) -> new GetUserInfoRes(
                rs.getString("nickName"),
                rs.getString("name"),
                rs.getString("profileImgUrl"),
                rs.getString("website"),
                rs.getString("introduction"),
                rs.getInt("postCount"),
                rs.getInt("followCount"),
                rs.getInt("followingCount")),
                selectUserInfoParam);
    }

    public List<GetUserPostsRes> selectUserPosts(int userIdx){
        String selectUserPostsQuery = "SELECT p.postIdx, pi.imgUrl as postImgUrl\n" +
                "FROM Post as p\n" +
                "    left join User as u on u.userIdx = p.userIdx\n" +
                "    left join PostImgUrl as pi on pi.postIdx = p.postIdx and pi.status = 'ACTIVE'\n" +
                "WHERE p.status = 'ACTIVE' and u.userIdx = ?\n" +
                "group by  p.postIdx;";
        int selectUserPostsParam = userIdx;

        return this.jdbcTemplate.query(selectUserPostsQuery, (rs, rowNum) -> new GetUserPostsRes(
                        rs.getInt("postIdx"),
                        rs.getString("postImgUrl")),
                selectUserPostsParam);
    }

    public int checkUserExist(int userIdx){
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx=?);";
        int checkUserExistParam = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery, int.class, checkUserExistParam);
    }


    public GetUserRes getUsersByIdx(int userIdx){
        String getUserByIdxQuery = "select userIdx,name,nickName,email from User where userIdx=?";
        int getUserByIdxParams = userIdx;
        return this.jdbcTemplate.queryForObject(getUserByIdxQuery,
                (rs, rowNum) -> new GetUserRes(
                        rs.getInt("userIdx"),
                        rs.getString("name"),
                        rs.getString("nickName"),
                        rs.getString("email")),
                getUserByIdxParams);
    }

    public int createUser(PostUserReq postUserReq){
        String createUserQuery = "insert into User (name, nickName, phone, email, password) VALUES (?,?,?,?,?)";
        Object[] createUserParams = new Object[]{postUserReq.getName(), postUserReq.getNickName(),postUserReq.getPhone(), postUserReq.getEmail(), postUserReq.getPassword()};
        this.jdbcTemplate.update(createUserQuery, createUserParams);

        String lastInserIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,int.class);
    }

    public int checkEmail(String email){
        String checkEmailQuery = "select exists(select email from User where email = ?)";
        String checkEmailParams = email;
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams);

    }

    public int modifyUserName(PatchUserReq patchUserReq){
        String modifyUserNameQuery = "update User set nickName = ? where userIdx = ? ";
        Object[] modifyUserNameParams = new Object[]{patchUserReq.getNickName(), patchUserReq.getUserIdx()};

        return this.jdbcTemplate.update(modifyUserNameQuery,modifyUserNameParams);
    }

    public int deleteUser(DeleteUserReq deleteUserReq){
        String deleteUserQuery = "delete from User where userIdx=?";
        int deleteUserParam = deleteUserReq.getUserIdx();

        return this.jdbcTemplate.update(deleteUserQuery, deleteUserParam);
    }




}
