package com.xzf.onlineq;

import com.xzf.onlineq.dao.QuestionDao;
import com.xzf.onlineq.dao.UserDao;
//import com.xzf.onlineq.service.UserService;
import com.xzf.onlineq.service.UserServiceNew;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InitDatabaseTest {
    @Autowired
    UserDao userDao;

    @Autowired
    QuestionDao questionDao;

    @Autowired
    UserServiceNew  userService;

    @Test
    public void userDaoTest() {
/*        Random random = new Random();
        for (int i = 0; i < 5; ++i) {
            User user = new User();
            user.setName("xxx" + random.nextInt(1000) + String.valueOf(i));
            user.setPassword(String.format("pw is : %d", random.nextInt(1000)));
            userDao.addUser(user);
        }*/
    }

    @Test
    public void userDaoTest2() {
/*        System.out.println(userDao.selectById(1).getName());
        User user = new User();
        user.setId(1);
        user.setPassword("change password");
        userDao.updatePassword(user);
        userDao.deleteById(1);*/
    }

    @Test
    public void questionDaoTest2() {
/*        Random random = new Random();
        for (int i = 0; i < 5; ++i) {
            Question question = new Question();
            question.setTitle("xxx" + random.nextInt(1000) + String.valueOf(i));
            question.setContent(UUID.randomUUID().toString());
            questionDao.addQuestion(question);
        }*/
    }

    @Test
    public void questionDaoTest() {
/*        List<Question> questions = questionDao.selectLatestQuestions(0, 0, 10);
        for (int i = 0; i < 10; ++i) {
            System.out.println(questions.get(i).getContent());
        }
        Assert.assertNotNull(questions.get(1));*/
    }

    @Test
    public void addUser(){
      /*  userService.register("关注通知","123456","123@qq.com");*/
    }
}
