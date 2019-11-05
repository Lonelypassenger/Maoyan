package com.stylefeng.guns.film;


import com.stylefeng.guns.film.vo.ActorVO;
import com.stylefeng.guns.film.vo.FilmDescVO;
import com.stylefeng.guns.film.vo.ImgVO;

import java.util.List;

/**
 * 因为我们原来接口里面的方法不都是需要异步的，所以我们将这些异步的方法提取出来。这个类用来实现异步操作
 * 原来的那个接口是同步操作。
 */

public interface FilmAsyncServiceApi {

    // 获取影片描述信息
    FilmDescVO getFilmDesc(String filmId);

    // 获取图片信息
    ImgVO getImgs(String filmId);

    // 获取导演信息
    ActorVO getDectInfo(String filmId);

    // 获取演员信息
    List<ActorVO> getActors(String filmId);

}
