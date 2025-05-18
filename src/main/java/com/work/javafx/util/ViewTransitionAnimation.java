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
        SLIDE_LEFT,     // 从左侧滑入
        SLIDE_RIGHT,    // 从右侧滑入
        SLIDE_TOP,      // 从顶部滑入
        SLIDE_BOTTOM,   // 从底部滑入
        ROTATE,         // 旋转过渡
        FLIP_HORIZONTAL, // 水平翻转
        FLIP_VERTICAL,  // 垂直翻转
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
            case SLIDE_LEFT://左
                playSlideAnimation(contentArea, newView, -1, 0);
                break;
            case SLIDE_RIGHT://右
                playSlideAnimation(contentArea, newView, 1, 0);
                break;
            case SLIDE_TOP://上
                playSlideAnimation(contentArea, newView, 0, -1);
                break;
            case SLIDE_BOTTOM://下
                playSlideAnimation(contentArea, newView, 0, 1);
                break;
            case ROTATE://旋转
                playRotateAnimation(contentArea, newView, 15);
                break;
            case FLIP_HORIZONTAL://水平翻转
                playFlipAnimation(contentArea, newView, true);
                break;
            case FLIP_VERTICAL://垂直翻转
                playFlipAnimation(contentArea, newView, false);
                break;
            case BOUNCE://弹性缩放
                playBounceAnimation(contentArea, newView);
                break;
        }
    }

    /**
     * 播放滑动动画
     * @param contentArea 内容区域容器
     * @param newView 新视图
     * @param directionX X方向滑动，-1为从左，1为从右，0为不滑动
     * @param directionY Y方向滑动，-1为从上，1为从下，0为不滑动
     */
    public static void playSlideAnimation(Pane contentArea, Parent newView, int directionX, int directionY) {
        // 设置新视图初始状态
        newView.setOpacity(0);
        double offsetX = directionX * contentArea.getWidth() * 0.3; // 只滑动30%的距离，更加微妙
        double offsetY = directionY * contentArea.getHeight() * 0.3;
        newView.setTranslateX(offsetX);
        newView.setTranslateY(offsetY);

        // 添加新视图到内容区
        contentArea.getChildren().add(newView);

        // 创建动画组
        ParallelTransition transition = new ParallelTransition();

        // 为旧视图创建动画（如果存在）
        if (!contentArea.getChildren().isEmpty() && contentArea.getChildren().size() > 1) {
            Parent oldView = (Parent) contentArea.getChildren().get(0);
            
            // 创建旧视图淡出动画
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldView);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setInterpolator(Interpolator.EASE_OUT);
            
            // 创建旧视图滑出动画（反方向）
            TranslateTransition translateOut = new TranslateTransition(Duration.millis(200), oldView);
            translateOut.setToX(-offsetX * 0.5);
            translateOut.setToY(-offsetY * 0.5);
            translateOut.setInterpolator(Interpolator.EASE_OUT);
            
            transition.getChildren().addAll(fadeOut, translateOut);
        }

        // 为新视图创建动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), newView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_IN);
        
        // 创建新视图滑入动画
        TranslateTransition translateIn = new TranslateTransition(Duration.millis(250), newView);
        translateIn.setFromX(offsetX);
        translateIn.setFromY(offsetY);
        translateIn.setToX(0);
        translateIn.setToY(0);
        translateIn.setInterpolator(Interpolator.EASE_OUT);
        
        transition.getChildren().addAll(fadeIn, translateIn);

        // 动画完成后执行
        transition.setOnFinished(event -> {
            if (contentArea.getChildren().size() > 1) {
                contentArea.getChildren().remove(0);
            }
            // 重置视图位置
            newView.setTranslateX(0);
            newView.setTranslateY(0);
        });
        
        transition.play();
    }
    
    /**
     * 播放旋转动画
     * @param contentArea 内容区域容器
     * @param newView 新视图
     * @param angle 旋转角度（微小角度效果最佳）
     */
    public static void playRotateAnimation(Pane contentArea, Parent newView, double angle) {
        // 设置新视图初始状态
        newView.setOpacity(0);
        newView.setRotate(angle);
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
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldView);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setInterpolator(Interpolator.EASE_OUT);
            
            // 创建旧视图旋转动画
            RotateTransition rotateOut = new RotateTransition(Duration.millis(200), oldView);
            rotateOut.setToAngle(-angle);
            rotateOut.setInterpolator(Interpolator.EASE_OUT);
            
            // 创建旧视图缩小动画
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), oldView);
            scaleOut.setToX(0.9);
            scaleOut.setToY(0.9);
            scaleOut.setInterpolator(Interpolator.EASE_OUT);
            
            transition.getChildren().addAll(fadeOut, rotateOut, scaleOut);
        }

        // 为新视图创建动画
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), newView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_IN);
        
        // 创建新视图旋转动画
        RotateTransition rotateIn = new RotateTransition(Duration.millis(250), newView);
        rotateIn.setFromAngle(angle);
        rotateIn.setToAngle(0);
        rotateIn.setInterpolator(Interpolator.EASE_OUT);
        
        // 创建新视图放大动画
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(250), newView);
        scaleIn.setFromX(0.95);
        scaleIn.setFromY(0.95);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);
        
        transition.getChildren().addAll(fadeIn, rotateIn, scaleIn);

        // 动画完成后执行
        transition.setOnFinished(event -> {
            if (contentArea.getChildren().size() > 1) {
                contentArea.getChildren().remove(0);
            }
            // 重置视图状态
            newView.setRotate(0);
            newView.setScaleX(1.0);
            newView.setScaleY(1.0);
        });
        
        transition.play();
    }
    
    /**
     * 播放翻页效果动画
     * @param contentArea 内容区域容器
     * @param newView 新视图
     * @param horizontal 是否水平翻转（true为水平，false为垂直）
     */
    public static void playFlipAnimation(Pane contentArea, Parent newView, boolean horizontal) {
        // 为翻转效果设置旋转轴
        Rotate rotate = new Rotate();
        rotate.setAxis(horizontal ? Rotate.Y_AXIS : Rotate.X_AXIS);
        rotate.setPivotX(horizontal ? contentArea.getWidth() / 2 : 0);
        rotate.setPivotY(horizontal ? 0 : contentArea.getHeight() / 2);
        
        // 设置新视图初始状态
        newView.setOpacity(0);
        newView.getTransforms().add(rotate);
        rotate.setAngle(90);

        // 添加新视图到内容区
        contentArea.getChildren().add(newView);

        // 创建动画时间线
        Timeline timeline = new Timeline();
        
        // 如果有旧视图，创建旧视图的翻转动画
        if (!contentArea.getChildren().isEmpty() && contentArea.getChildren().size() > 1) {
            Parent oldView = (Parent) contentArea.getChildren().get(0);
            
            // 为旧视图设置旋转轴
            Rotate oldRotate = new Rotate();
            oldRotate.setAxis(horizontal ? Rotate.Y_AXIS : Rotate.X_AXIS);
            oldRotate.setPivotX(horizontal ? contentArea.getWidth() / 2 : 0);
            oldRotate.setPivotY(horizontal ? 0 : contentArea.getHeight() / 2);
            oldView.getTransforms().add(oldRotate);
            
            // 旧视图的翻转动画
            KeyValue kv1 = new KeyValue(oldRotate.angleProperty(), 0, Interpolator.EASE_IN);
            KeyValue kv2 = new KeyValue(oldRotate.angleProperty(), -90, Interpolator.EASE_IN);
            KeyValue kv3 = new KeyValue(oldView.opacityProperty(), 1, Interpolator.EASE_IN);
            KeyValue kv4 = new KeyValue(oldView.opacityProperty(), 0, Interpolator.EASE_IN);
            
            KeyFrame kf1 = new KeyFrame(Duration.ZERO, kv1, kv3);
            KeyFrame kf2 = new KeyFrame(Duration.millis(250), kv2, kv4);
            
            timeline.getKeyFrames().addAll(kf1, kf2);
        }
        
        // 新视图的翻转动画
        KeyValue kv5 = new KeyValue(rotate.angleProperty(), 90, Interpolator.EASE_OUT);
        KeyValue kv6 = new KeyValue(rotate.angleProperty(), 0, Interpolator.EASE_OUT);
        KeyValue kv7 = new KeyValue(newView.opacityProperty(), 0, Interpolator.EASE_OUT);
        KeyValue kv8 = new KeyValue(newView.opacityProperty(), 1, Interpolator.EASE_OUT);
        
        KeyFrame kf3 = new KeyFrame(Duration.millis(250), kv5, kv7);
        KeyFrame kf4 = new KeyFrame(Duration.millis(500), kv6, kv8);
        
        timeline.getKeyFrames().addAll(kf3, kf4);
        
        // 动画完成后执行
        timeline.setOnFinished(event -> {
            if (contentArea.getChildren().size() > 1) {
                contentArea.getChildren().remove(0);
            }
            // 移除变换以避免影响后续操作
            newView.getTransforms().clear();
        });
        
        timeline.play();
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