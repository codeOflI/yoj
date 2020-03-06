package com.yoj.custom.judge.util.impl;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import com.yoj.custom.judge.bean.ExecuteMessage;
import com.yoj.custom.judge.util.ExecutorUtil;
import com.yoj.custom.properties.JudgeProperties;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * @author lmz 远程连接linux使用
 */
@ToString
@Slf4j
public class RemoteExecutor implements ExecutorUtil {

    @Autowired
    JudgeProperties judgeProperties;
    //    private static final Logger log = LoggerFactory.getLogger(RemoteExecutor.class);
    private static Connection conn;

    /**
     * 登录主机
     *
     * @return 登录成功返回true，否则返回false
     */
    @PostConstruct
    public void login() {
        String ip = judgeProperties.getWindows().getIp();
        String userName = judgeProperties.getWindows().getUserName();
        String userPwd = judgeProperties.getWindows().getPassword();
        this.conn = null;
        try {
            conn = new Connection(ip);
            conn.connect();// 连接
            boolean flg = conn.authenticateWithPassword(userName, userPwd);// 认证
            if (flg) {
                RemoteExecutor.conn = conn;
                log.info("=========登录成功=========" + conn);
            }
        } catch (IOException e) {
            log.error("=========登录失败=========" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     *
     */
    @Override
    public ExecuteMessage execute(String cmd) {
        Session session = null;
        try {
            // can not find closed connection
            session = conn.openSession();// 打开一个会话
            session.execCommand(cmd);// 执行命令
        } catch (IOException e) {
            log.info("执行命令失败,链接conn:" + conn + ",执行的命令：" + cmd + "  " + e.getMessage());
            return new ExecuteMessage(e.getMessage(), null);
        }
        ExecuteMessage result = new ExecuteMessage();
        try {
            result.setError(message(session.getStderr()));
            result.setStdout(message(session.getStdout()));
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
