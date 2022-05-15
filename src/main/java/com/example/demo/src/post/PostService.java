package com.example.demo.src.post;


import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponseStatus;
import com.example.demo.src.post.model.PatchPostsReq;
import com.example.demo.src.post.model.PostPostsReq;
import com.example.demo.src.post.model.PostPostsRes;
import com.example.demo.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    private final PostDao postDao;
    private final PostProvider postProvider;
    private final JwtService jwtService;

    @Autowired
    public PostService(PostDao postDao, JwtService jwtService, PostProvider postProvider){
        this.postDao = postDao;
        this.postProvider = postProvider;
        this.jwtService = jwtService;
    }

    public PostPostsRes createPosts(int userIdx, PostPostsReq postPostsReq) throws BaseException {
        try {

            int postIdx = postDao.insertPosts(userIdx, postPostsReq.getContent());

            for (int i = 0; i < postPostsReq.getPostImgUrls().size(); ++i){
                postDao.insertPostImgs(postIdx, postPostsReq.getPostImgUrls().get(i));
            }

            return new PostPostsRes(postIdx);
        }
        catch (Exception e){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public void modifyPost(int userIdx, int postIdx, PatchPostsReq patchPostsReq) throws BaseException{
        if(postProvider.checkUserExist(userIdx) == 0){
            throw new BaseException(BaseResponseStatus.USERS_EMPTY_USER_ID);
        }
        else if(postProvider.checkPostExist(postIdx) == 0){
            throw new BaseException(BaseResponseStatus.POST_EMPTY_POST_ID);
        }

        try{
            int result = postDao.updatePost(postIdx, patchPostsReq.getContent());
            if(result == 0){
                throw new BaseException(BaseResponseStatus.MODIFY_FAIL_POST);
            }
        }
        catch (Exception e){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public void deletePost(int postIdx) throws BaseException{
        String test = new String(postDao.getPostStatus(postIdx));
        if(test == "INACTIVE"){
            throw new BaseException(BaseResponseStatus.POST_STATUS_INACTIVE);
        }
        try{
            postDao.deletePost(postIdx);

        }
        catch (Exception e){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

}
