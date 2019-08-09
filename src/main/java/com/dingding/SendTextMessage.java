package com.dingding;

import com.RobotClient;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class SendTextMessage {
    //机器人消息token
    public static String WEBHOOK_TOKEN = "https://oapi.dingtalk.com/robot/send?access_token=f742323efc2349beb658593b5dc68f53d37b43706f9b910c8439ddae78ec7d17";

    private static RobotClient robot = new RobotClient();

    /**
     * 发送普通文本消息
     *
     * @param message
     * @return
     */
    public static SendResult send(String message) {
        TextMessage textMessage = new TextMessage(message);
        SendResult sendResult = null;
        try {
            sendResult = robot.send(WEBHOOK_TOKEN, textMessage);
        } catch (Exception e) {
            log.error("===> send robot message error:", sendResult);
        }
        return sendResult;
    }

    /**
     * 发送文本消息 可以@部分人
     *
     * @param message
     * @param atMobiles 要@人的电话号码 ArrayList<String>
     * @return
     */
    public static SendResult sendWithAt(String message, ArrayList<String> atMobiles) {
        TextMessage textMessage = new TextMessage(message);
        SendResult sendResult = null;
        textMessage.setAtMobiles(atMobiles);
        try {
            sendResult = robot.send(WEBHOOK_TOKEN, textMessage);
        } catch (Exception e) {
            log.error("===> send robot message atPeople error:", sendResult);
        }
        return sendResult;
    }

    /**
     * 发送文本消息 并@所有人
     *
     * @param message
     * @return
     */
    public static SendResult sendWithAtAll(String message) {
        TextMessage textMessage = new TextMessage(message);
        SendResult sendResult = null;
        textMessage.setIsAtAll(true);
        try {
            sendResult = robot.send(WEBHOOK_TOKEN, textMessage);
        } catch (Exception e) {
            log.error("===> send robot message atAll error:", sendResult);
        }
        return sendResult;
    }




}