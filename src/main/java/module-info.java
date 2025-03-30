module com.work.javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.google.gson;
    requires java.logging;
    
    // 打印支持
    requires javafx.swing;
    
    // 导出 DataResponse 包到 Gson 模块
    exports com.work.javafx.DataResponse to com.google.gson;
    // 如果Gson需要反射访问私有字段（例如没有公共getter/setter时），需开放包
    opens com.work.javafx.DataResponse to com.google.gson;
    opens com.work.javafx to javafx.fxml;
    exports com.work.javafx;
    exports com.work.javafx.controller;
    opens com.work.javafx.controller to javafx.fxml;
    
    // 导出util包
    exports com.work.javafx.util;
    exports com.work.javafx.controller.student;
    opens com.work.javafx.controller.student to javafx.fxml;
    exports com.work.javafx.controller.teacher;
    opens com.work.javafx.controller.teacher to javafx.fxml;
}