package org.example;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.java.Log;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;

import java.util.List;
import java.util.logging.Level;

@Contract(
        name = "CourseContract",
        info = @Info(
                title = "Course contract",
                description = "The hyperlegendary course contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "f.carr@example.com",
                        name = "F Carr",
                        url = "https://hyperledger.example.com")))
@Default
@Log
public class CourseContract implements ContractInterface {


    @Transaction
    public void initLedger(final Context ctx) {

        ChaincodeStub stub = ctx.getStub();
        for (int i = 0; i < 10; i++) {
            Course course = new Course().setCourseKey("Course"+i+"_Org1")
                    .setCourseName("math" + i)
                    .setCourseObjectives("Objectives 1")
                    .setEmployerEvaluation("good");
            stub.putStringState(course.getCourseKey(), JSON.toJSONString(course));
        }

    }

    @Transaction
    public Course queryCourse(final Context ctx, final String courseKey) {

        ChaincodeStub stub = ctx.getStub();
        String courseState = stub.getStringState(courseKey);

        if (StringUtils.isBlank(courseState)) {
            String errorMessage = String.format("CourseKey %s does not exist", courseKey);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return JSON.parseObject(courseState , Course.class);
    }

    //couchDB富查询//
    @Transaction
    public CourseQueryResultList queryAllCourse(final Context ctx) {
        String query = String.format("{\"selector\":{\"_id\":{\"$gt\":null}}}");

        CourseQueryResultList courseQueryResultList = new CourseQueryResultList();
        QueryResultsIterator<KeyValue> queryResult = ctx.getStub().getQueryResult(query);
        List<CourseQueryResult> courses = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                courses.add(new CourseQueryResult().setKey(kv.getKey()).setCourse(JSON.parseObject(kv.getStringValue() , Course.class)));
            }
            courseQueryResultList.setCourses(courses);
        }
        return courseQueryResultList;
    }

    @Transaction
    public Course createCourse(final Context ctx, final String courseKey, String courseName, String courseObjectives, String employerEvaluation) {

        ChaincodeStub stub = ctx.getStub();
        String courseState = stub.getStringState(courseKey);

        if (StringUtils.isNotBlank(courseState)) {
            String errorMessage = String.format("CourseKey %s already exists", courseKey);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Course course = new Course()
                .setCourseKey(courseKey)
                .setCourseName(courseName)
                .setCourseObjectives(courseObjectives)
                .setEmployerEvaluation(employerEvaluation);

        String json = JSON.toJSONString(course);
        stub.putStringState(courseKey, json);

        stub.setEvent("createCourseEvent", org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));
        return course;
    }

    @Transaction
    public Course updateCourse(final Context ctx, final String courseKey, String courseName, String courseObjectives, String employerEvaluation) {

        ChaincodeStub stub = ctx.getStub();
        String courseState = stub.getStringState(courseKey);

        if (StringUtils.isBlank(courseState)) {
            String errorMessage = String.format("CourseKey %s does not exist", courseKey);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Course course = new Course()
                .setCourseKey(courseKey)
                .setCourseName(courseName)
                .setCourseObjectives(courseObjectives)
                .setEmployerEvaluation(employerEvaluation);

        stub.putStringState(courseKey, JSON.toJSONString(course));

        return course;
    }

    @Transaction
    public Course deleteCourse(final Context ctx, final String courseKey) {

        ChaincodeStub stub = ctx.getStub();
        String courseState = stub.getStringState(courseKey);

        if (StringUtils.isBlank(courseState)) {
            String errorMessage = String.format("CourseKey %s does not exist", courseKey);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        stub.delState(courseKey);

        return JSON.parseObject(courseState, Course.class);
    }

    //私有数据//
    @Transaction
    public PrivateData createPrivateData(final Context ctx, final String collection , final String username , String password) {

        ChaincodeStub stub = ctx.getStub();

        String userState = stub.getPrivateDataUTF8(collection , username);

        if (StringUtils.isNotBlank(userState)) {
            String errorMessage = String.format("Current Username Data %s already exists", username);
            log.log(Level.WARNING , errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        PrivateData user = new PrivateData()
                .setUsername(username)
                .setPassword(password)
                .setCollection(collection);

        String json = JSON.toJSONString(user);

        log.info(String.format("要保存的数据 %s" , json));

        stub.putPrivateData(collection , username , json);

        return user;
    }

    //形参中的 final String collection 是 collections_config.json 文件中name属性对应的value的值
    @Transaction
    public PrivateData queryPrivateData(final Context ctx, final String collection , final String username) {

        ChaincodeStub stub = ctx.getStub();

        String userState = stub.getPrivateDataUTF8(collection , username);

        if (StringUtils.isBlank(userState)) {
            String errorMessage = String.format("Private User %s does not exist", username);
            log.log(Level.WARNING , errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return JSON.parseObject(userState , PrivateData.class);
    }

//    @Transaction
//    public PrivateData updatePrivateCat(final Context ctx, final String collection, final String key , String name , Integer age , String color , String breed) {
//
//        ChaincodeStub stub = ctx.getStub();
//        log.info(String.format("更新私有数据 , collection [%s] , mspId [%s] , key [%s] , name [%s] age [%s] color [%s] breed [%s] " , collection,stub.getMspId() , key , name , age , color , breed));
//
//        String catState = stub.getPrivateDataUTF8(collection , key);
//
//        if (StringUtils.isBlank(catState)) {
//            String errorMessage = String.format("Private Cat %s does not exist", key);
//            log.log(Level.WARNING , errorMessage);
//            throw new ChaincodeException(errorMessage);
//        }
//
//        PrivateData cat = new PrivateData()
//                .setCourse(new Course().setName(name)
//                        .setAge(age)
//                        .setBreed(breed)
//                        .setColor(color))
//                .setCollection(collection);
//
//        String json = JSON.toJSONString(cat);
//
//        log.info(String.format("要保存的数据 %s" , json));
//
//        stub.putPrivateData(collection , key , json);
//
//        return cat;
//    }
//
//    @Transaction
//    public PrivateData deletePrivateCat(final Context ctx, final String collection , final String key) {
//
//        ChaincodeStub stub = ctx.getStub();
//
//        log.info(String.format("删除私有数据 , collection [%s] , mspId [%s] , key [%s] " , collection , stub.getMspId() , key));
//
//        String catState = stub.getPrivateDataUTF8(collection , key);
//
//        if (StringUtils.isBlank(catState)) {
//            String errorMessage = String.format("Private Cat %s does not exist", key);
//            log.log(Level.WARNING , errorMessage);
//
//            throw new ChaincodeException(errorMessage);
//        }
//
//        stub.delPrivateData(collection , key);
//
//        return JSON.parseObject(catState , PrivateData.class);
//    }
//
//
//
    @Transaction
    public CourseQueryPageResult queryCoursePageByName(final Context ctx, String courseName , Integer pageSize , String bookmark) {

        log.info(String.format("使用 courseName 分页查询 Course , courseName = %s" , courseName));

        String query = String.format("{\"selector\":{\"courseName\":\"%s\"}}", courseName);

        log.info(String.format("query string = %s" , query));

        //分页查询只是调的API不同，具体调的是getQueryResultWithPagination()
        ChaincodeStub stub = ctx.getStub();
        QueryResultsIteratorWithMetadata<KeyValue> queryResult = stub.getQueryResultWithPagination(query, pageSize, bookmark);

        List<CourseQueryResult> courses = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                courses.add(new CourseQueryResult().setKey(kv.getKey()).setCourse(JSON.parseObject(kv.getStringValue() , Course.class)));
            }
        }

        return new CourseQueryPageResult()
                .setCourses(courses)
                .setBookmark(queryResult.getMetadata().getBookmark());
    }

//    @Transaction
//    public CatQueryResultList queryCatByNameAndColor(final Context ctx, String name , String color) {
//
//        log.info(String.format("使用 name & color 查询 cat , name = %s , color = %s" , name , color));
//
//        String query = String.format("{\"selector\":{\"name\":\"%s\" , \"color\":\"%s\"} , \"use_index\":[\"_design/indexNameColorDoc\", \"indexNameColor\"]}", name , color);
//        return queryCat(ctx.getStub() , query);
//    }
//
//    private CatQueryResultList queryCat(ChaincodeStub stub , String query) {
//
//        CatQueryResultList resultList = new CatQueryResultList();
//        QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(query);
//        List<CatQueryResult> results = Lists.newArrayList();
//
//        if (! IterableUtils.isEmpty(queryResult)) {
//            for (KeyValue kv : queryResult) {
//                results.add(new CatQueryResult().setKey(kv.getKey()).setCourse(JSON.parseObject(kv.getStringValue() , Course.class)));
//            }
//            resultList.setCats(results);
//        }
//
//        return resultList;
//    }
//
}
