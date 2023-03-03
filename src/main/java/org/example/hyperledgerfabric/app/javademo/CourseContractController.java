package org.example.hyperledgerfabric.app.javademo;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Slf4j
@AllArgsConstructor
public class CourseContractController {
    @Autowired
    private Gateway Org1Gateway;
    @Autowired
    private Gateway Org2Gateway;


    public Contract getOrg1Contract() {
        return Org1Gateway.getNetwork("businesschannel").getContract("hyperledger-fabric-contract-java-demo" , "CourseContract");
    }
    public Contract getOrg2Contract() {
        return Org2Gateway.getNetwork("businesschannel").getContract("hyperledger-fabric-contract-java-demo" , "CourseContract");
    }

    @PostMapping("/users/")
    public Map<String, Object> login(@RequestBody PrivateDataDTO privateDataDTO) {
        Map<String, Object> result = Maps.newConcurrentMap();
        byte[] course = new byte[0];
        //Org1登录
        if(privateDataDTO.getCollection().equals("collectionOrg1Users")) {
            try {
                 course = getOrg1Contract().evaluateTransaction("queryPrivateData", privateDataDTO.getCollection(), privateDataDTO.getUsername());
            }catch (Exception e){
                if(Arrays.asList(e.getMessage().split(" ")).contains("exist")) {
                    result.put("payload", "登录失败,Org1中此用户不存在");
                    result.put("role", "Org2");
                    result.put("status", "500");
                    return result;
                }
            }
            String queryResult = StringUtils.newStringUtf8(course).replaceAll("\\\\", "");
            if (privateDataDTO.getPassword().equals(JSONObject.parseObject(queryResult).get("password"))) {
                result.put("payload", "登录成功");
                result.put("role","Org1");
                result.put("status", "200");
            } else {
                result.put("payload", "登录失败,密码错误");
                result.put("status", "500");
            }
        //Org2登录
        }else {
            try {
                course = getOrg2Contract().evaluateTransaction("queryPrivateData", privateDataDTO.getCollection(), privateDataDTO.getUsername());
            }catch (Exception e){
                System.out.println(e);
                if(Arrays.asList(e.getMessage().split(" ")).contains("exist")){
                    result.put("payload", "登录失败,Org2中用户不存在");
                    result.put("role","Org2");
                    result.put("status", "500");
                    return result;
                }
            }
            String queryResult = StringUtils.newStringUtf8(course).replaceAll("\\\\", "");
            if (privateDataDTO.getPassword().equals(JSONObject.parseObject(queryResult).get("password"))) {
                result.put("payload", "登录成功");
                result.put("role","Org2");
                result.put("status", "200");
            } else {
                result.put("payload", "登录失败,密码错误");
                result.put("status", "500");
            }
        }
        return result;
    }

    @GetMapping("/courses/{key}")
    public ArrayList<Map> queryCourseByCourseKey(@PathVariable String key) throws GatewayException {
        Map<String, Object> result = Maps.newConcurrentMap();
        ArrayList<Map> arrayList = new ArrayList<>();
        byte[] course = new byte[0];

        if (key.split("_",2)[1].equals("Org1")) {
            try {
                course = getOrg1Contract().evaluateTransaction("queryCourse", key);
            }catch (Exception e){
                if(Arrays.asList(e.getMessage().split(" ")).contains("exist")) {
                    result.put("payload", "查询失败,你查询的关键字不存在");
                    result.put("status", "500");
                    arrayList.add(result);
                    return arrayList;
                }
            }
        }
        else {
            course = getOrg2Contract().evaluateTransaction("queryCourse", key);
        }
        String queryResult = StringUtils.newStringUtf8(course).replaceAll("\\\\","");
        result.put("payload", queryResult);
        result.put("status", "ok,querySuccess");

        arrayList.add(JSONObject.parseObject(queryResult));
        return arrayList;
    }

    @GetMapping("/courses/")
    public Map<String, Object> queryAllCourse() throws GatewayException {
        Map<String, Object> result = Maps.newConcurrentMap();
        byte[] course = new byte[0];
                course = getOrg1Contract().evaluateTransaction("queryAllCourse");

        String queryResult = StringUtils.newStringUtf8(course).replaceAll("\\\\", "");
        result.put("payload", JSONObject.parseObject(queryResult));
        result.put("status", "ok,queryAllCourse");
        return result;
    }

    @PostMapping("/courses/")
    public Map<String, Object> createCourse(@RequestBody CourseDTO course) throws Exception {
        Map<String, Object> result = Maps.newConcurrentMap();
        if (course.getCourseKey().split("_")[1].equals("Org1")) {
            byte[] bytes = getOrg1Contract().submitTransaction("createCourse", course.getCourseKey(), course.getCourseName(), course.getCourseObjectives(), course.getEmployerEvaluation()==null?"":course.getEmployerEvaluation());
            result.put("payload", StringUtils.newStringUtf8(bytes));
            result.put("status", "ok,createSuccess");
        }else {
            result.put("payload", "You Org2, No permission to create");
            result.put("status", "ok,No permission to create");
        }

        return result;
    }

    @PutMapping("/courses/")
    public Map<String, Object> updateCourse(@RequestBody CourseDTO course) throws Exception {
        Map<String, Object> result = Maps.newConcurrentMap();
        if (course.getCourseKey().split("_", 2)[1].equals("Org1")) {
            byte[] bytes = getOrg1Contract().submitTransaction("updateCourse",course.getCourseKey(), course.getCourseName(),course.getCourseObjectives(), course.getEmployerEvaluation());
            result.put("payload", StringUtils.newStringUtf8(bytes));
            result.put("status", "ok,updateSuccess,Org1");
        }else {
            byte[] bytes = getOrg1Contract().submitTransaction("updateCourse", course.getCourseKey(), course.getCourseName(),course.getCourseObjectives(), course.getEmployerEvaluation());
            result.put("payload", StringUtils.newStringUtf8(bytes));
            result.put("status", "ok,updateSuccess,Org2");
        }

        return result;
    }

    @DeleteMapping("/courses/{key}")
    public Map<String, Object> deleteCourseByCourseKey(@PathVariable String key) throws Exception {
        Map<String, Object> result = Maps.newConcurrentMap();

        if (key.split("_", 2)[1].equals("Org1")) {
            byte[] bytes = getOrg1Contract().submitTransaction("deleteCourse", key);
            result.put("payload", StringUtils.newStringUtf8(bytes));
            result.put("status", "ok,deleteSuccess,Org1");
        }else {
            result.put("payload", "Org2 no permission to del");
            result.put("status", "ok,deleteError,Org2 no permission to del");
        }
        return result;
    }

    //仅管理员可创建用户
    @PostMapping("/private/")
    public Map<String, Object> createPrivateData(@RequestBody PrivateDataDTO privateDataDTO) throws Exception {

        Map<String, Object> result = Maps.newConcurrentMap();
        if(privateDataDTO.getCollection().equals("collectionOrg1Users")){
            byte[] bytes = getOrg1Contract().newProposal("createPrivateData")
                    .addArguments(privateDataDTO.getCollection(), privateDataDTO.getUsername(), privateDataDTO.getPassword())
                    .build()
                    .endorse()
                    .submit();
            result.put("payload", StringUtils.newStringUtf8(bytes));
            result.put("status", "ok,createPrivateData()");
            return result;
        }else if(privateDataDTO.getCollection().equals("collectionOrg2Users")){
            byte[] bytes = getOrg2Contract().newProposal("createPrivateData")
                    .addArguments(privateDataDTO.getCollection(), privateDataDTO.getUsername(), privateDataDTO.getPassword())
                    .build()
                    .endorse()
                    .submit();
            result.put("payload", StringUtils.newStringUtf8(bytes));
            result.put("status", "ok,createPrivateData()");
            return result;
        }
        result.put("payload", "组织不存在");
        result.put("status", "500");
        return result;
    }

    //测试阶段.注意GetMapping中{}中的路径变量要和相应PathVariable接收的路径变量的名字必须一样
    @GetMapping("/private/{courseName}/{pageSize}")
    public Map<String, Object> queryCoursePageByName(@PathVariable("courseName") String courseName ,@PathVariable("pageSize") String pageSize , String bookmark) throws Exception {
        bookmark = "";
        Map<String, Object> result = Maps.newConcurrentMap();

        byte[] bytes = getOrg1Contract().evaluateTransaction("queryCoursePageByName",courseName, pageSize,bookmark);

        result.put("payload", StringUtils.newStringUtf8(bytes));
        result.put("status", "ok,queryCoursePageByName()");
        return result;
    }


//
//    @PostMapping("/private/")
//    public Map<String, Object> updatePrivateCat(@RequestBody PrivateCatDTO privateCat) throws Exception {
//
//        Map<String, Object> result = Maps.newConcurrentMap();
//
//        CatDTO cat = privateCat.getCat();
//        byte[] bytes = contract.submitTransaction("updatePrivateCat" ,privateCat.getCollection() , cat.getKey(), cat.getName(), String.valueOf(cat.getAge()), cat.getColor(), cat.getBreed());
//
//        result.put("payload", StringUtils.newStringUtf8(bytes));
//        result.put("status", "ok,updatePrivateCat()");
//
//        return result;
//    }
//
//    @DeleteMapping("/private/{collection}/{key}")
//    public Map<String, Object> deletePrivateCatByKey(@PathVariable String collection , @PathVariable String key) throws Exception {
//
//        Map<String, Object> result = Maps.newConcurrentMap();
//        byte[] cat = contract.evaluateTransaction("deletePrivateCat", collection , key);
//        contract.submitTransaction("deletePrivateCat" ,collection , key);
//
//        result.put("payload", StringUtils.newStringUtf8(cat));
//        result.put("status", "ok,deletePrivateCatByKey()");
//
//        return result;
//    }
}
