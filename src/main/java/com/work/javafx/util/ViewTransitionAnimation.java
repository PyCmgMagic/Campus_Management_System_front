package com.work.javafx.util;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 视图切换动画工具类
 * 提供页面切换时的动画效果
 */
public class ViewTransitionAnimation {

    /**
     * 定义动画类型枚举
     */
    public enum AnimationType {
        FADE_SCALE,     // 淡入淡出+缩放（默认）
        BOUNCE          // 弹性效果
    }

    /**
     * 应用视图切换动画
     * @param contentArea 内容区域容器
     * @param newView 新视图
     * @param callback 动画完成后的回调函数
     * @return ParallelTransition 动画对象，可用于控制播放
     */
    public static ParallelTransition applyAnimation(Pane contentArea, Parent newView, Runnable callback) {
        // 设置新视图初始状态
        newView.setOpacity(0);
        newView.setScaleX(0.97);
        newView.setScaleY(0.97);

        // 添加新视图到内容区，但不移除旧视图
        contentArea.getChildren().add(newView);

        // 创建动画组
        ParallelTransition transition = new ParallelTransition();
        
        // 为旧视图创建动画（如果存在）
        if (!contentArea.getChildren().isEmpty() && contentArea.getChildren().size() > 1) {
            Parent oldView = (Parent) contentArea.getChildren().get(0);
            
            // 创建旧视图淡出动画
            FadeTransition fadeOut = new FadeTransition(Duration.millis(180), oldView);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setInterpolator(Interpolator.EASE_OUT);
            
            // 创建旧视图轻微缩小动画
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(180), oldView);
            scaleOut.setToX(0.96);
            scaleOut.setToY(0.96);
            scaleOut.setInterpolator(Interpolator.EASE_OUT);
            
            transition.getChildren().addAll(fadeOut, scaleOut);
        }

        // 为新视图创建动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), newView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_IN);
        
        // 创建新视图轻微放大动画
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(220), newView);
        scaleIn.setFromX(0.97);
        scaleIn.setFromY(0.97);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);
        
        transition.getChildren().addAll(fadeIn, scaleIn);

        // 动画完成后执行回调
        transition.setOnFinished(event -> {
            // 只保留最新添加的视图
            if (contentArea.getChildren().size() > 1) {
                contentArea.getChildren().remove(0);
            }
            // 重置视图状态
            newView.setScaleX(1.0);
            newView.setScaleY(1.0);
            
            // 执行回调
            if (callback != null) {
                callback.run();
            }
        });

        return transition;
    }
    
    /**
     * 应用视图切换动画并自动播放
     * @param contentArea 内容区域容器
     * @param newView 新视图
     * @param callback 动画完成后的回调函数
     */
    public static void playAnimation(Pane contentArea, Parent newView, Runnable callback) {
        ParallelTransition transition = applyAnimation(contentArea, newView, callback);
        transition.play();
    }
    
    /**
     * 应用视图切换动画并自动播放（无回调版本）
     * @param contentArea 内容区域容器
     * @param newView 新视图
     */
    public static void playAnimation(Pane contentArea, Parent newView) {
        playAnimation(contentArea, newView, null);
    }
    
    /**
     * 自定义动画参数版本
     * @param contentArea 内容区域容器
     * @param newView 新视图
     * @param duration 动画持续时间（毫秒）
     * @param scaleRatio 缩放比例
     * @param callback 回调函数
     */
    public static void playAnimationCustom(Pane contentArea, Parent newView, 
                                          double duration, double scaleRatio, 
                                          Runnable callback) {
        // 设置新视图初始状态
        newView.setOpacity(0);
        newView.setScaleX(1 - scaleRatio);
        newView.setScaleY(1 - scaleRatio);

        // 添加新视图到内容区
        contentArea.getChildren().add(newView);

        // 创建动画组
        ParallelTransition transition = new ParallelTransition();
        
        // 处理旧视图
        if (!contentArea.getChildren().isEmpty() && contentArea.getChildren().size() > 1) {
            Parent oldView = (Parent) contentArea.getChildren().get(0);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(duration * 0.8), oldView);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setInterpolator(Interpolator.EASE_OUT);
            
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(duration * 0.8), oldView);
            scaleOut.setToX(1 - scaleRatio - 0.01);
            scaleOut.setToY(1 - scaleRatio - 0.01);
            scaleOut.setInterpolator(Interpolator.EASE_OUT);
            
            transition.getChildren().addAll(fadeOut, scaleOut);
        }

        // 新视图动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(duration), newView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_IN);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(duration), newView);
        scaleIn.setFromX(1 - scaleRatio);
        scaleIn.setFromY(1 - scaleRatio);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);
        
        transition.getChildren().addAll(fadeIn, scaleIn);
        
        // 动画完成回调
        transition.setOnFinished(event -> {
            if (contentArea.getChildren().size() > 1) {
                contentArea.getChildren().remove(0);
            }
            newView.setScaleX(1.0);
            newView.setScaleY(1.0);
            
            if (callback != null) {
                callback.run();
            }
        });
        
        transition.play();
    }

    /**
     * 播放指定类型的动画效果
     * @param contentArea 内容区域容器
     * @param newView 新视图
     * @param type 动画类型
     */
    public static void playAnimationWithType(Pane contentArea, Parent newView, AnimationType type) {
        switch (type) {
            case FADE_SCALE://缩放
                playAnimation(contentArea, newView);
                break;
            case BOUNCE://弹性缩放
                playBounceAnimation(contentArea, newView);
                break;
        }
    }


    
    /**
     * 弹性过渡动画效果
     * @param contentArea 内容区域容器
     * @param newView 新视图
     */
    public static void playBounceAnimation(Pane contentArea, Parent newView) {
        // 设置新视图初始状态
        newView.setOpacity(0);
        newView.setScaleX(0.95);
        newView.setScaleY(0.95);

        // 添加新视图到内容区
        contentArea.getChildren().add(newView);

        // 创建动画组
        ParallelTransition transition = new ParallelTransition();

        // 为旧视图创建动画（如果存在）
        if (!contentArea.getChildren().isEmpty() && contentArea.getChildren().size() > 1) {
            Parent oldView = (Parent) contentArea.getChildren().get(0);
            
            // 创建旧视图淡出动画
            FadeTransition fadeOut = new FadeTransition(Duration.millis(180), oldView);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setInterpolator(Interpolator.EASE_OUT);
            
            // 创建旧视图缩小动画
            Timeline scaleOutTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(oldView.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                    new KeyValue(oldView.scaleYProperty(), 1.0, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(100), 
                    new KeyValue(oldView.scaleXProperty(), 1.03, Interpolator.EASE_OUT),
                    new KeyValue(oldView.scaleYProperty(), 1.03, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.millis(180), 
                    new KeyValue(oldView.scaleXProperty(), 0.95, Interpolator.EASE_OUT),
                    new KeyValue(oldView.scaleYProperty(), 0.95, Interpolator.EASE_OUT))
            );
            
            transition.getChildren().addAll(fadeOut, scaleOutTimeline);
        }

        // 为新视图创建动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(350), newView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_IN);
        
        // 创建新视图弹性缩放动画
        Timeline scaleInTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(newView.scaleXProperty(), 0.97, Interpolator.SPLINE(0.25, 0.1, 0.25, 1)),
                new KeyValue(newView.scaleYProperty(), 0.97, Interpolator.SPLINE(0.25, 0.1, 0.25, 1))),
            new KeyFrame(Duration.millis(250), 
                new KeyValue(newView.scaleXProperty(), 1.01, Interpolator.SPLINE(0.25, 0.1, 0.25, 1)),
                new KeyValue(newView.scaleYProperty(), 1.01, Interpolator.SPLINE(0.25, 0.1, 0.25, 1))),
            new KeyFrame(Duration.millis(350), 
                new KeyValue(newView.scaleXProperty(), 1.0, Interpolator.SPLINE(0.25, 0.1, 0.25, 1)),
                new KeyValue(newView.scaleYProperty(), 1.0, Interpolator.SPLINE(0.25, 0.1, 0.25, 1)))
        );
        
        transition.getChildren().addAll(fadeIn, scaleInTimeline);

        // 动画完成后执行
        transition.setOnFinished(event -> {
            if (contentArea.getChildren().size() > 1) {
                contentArea.getChildren().remove(0);
            }
            // 重置视图状态
            newView.setScaleX(1.0);
            newView.setScaleY(1.0);
        });
        
        transition.play();
    }
} 