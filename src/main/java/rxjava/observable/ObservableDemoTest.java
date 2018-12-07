package rxjava.observable;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.Callable;

/**
 * @author luxuanyu
 * @version 1.0
 * @desc 此类主要演示observable对象的创建
 * @since 2018/12/7 10:18
 */
@RunWith(JUnit4.class)
public class ObservableDemoTest {

    /**
     * create(ObservableOnSubscribe<T>)创建一个observable对象
     *
     * 指定被观察者的任务执行线程
     * subscribeOn(Schedulers schedulers) eg:subscribeOn(Schedulers.newThread())
     *
     * 指定观察者回调方法的线程，onSubcribe的线程是执行subscribe方法的线程而不是observeOn指定的线程
     * observeOn(Schedulers schedulers) eg:observeOn(Schedulers.io())
     *
     * subscribeOn(Schedulers.newThread())异步执行任务，不会阻塞，System.out.println("create end.....");会提前输出，不指定任务线程则create end则最后输出
     *
     *  指定observeOn(Schedulers.newThread(),true)如果delyError不指定为true，任务发送异常时onNext不会回调，但是onError会回调。
     */
    @Test
    public void create(){

        //消息发布者，被观察者
        Observable.create(new ObservableOnSubscribe<String>() {

            @Override
            public void subscribe(final ObservableEmitter<String> observableEmitter) throws Exception {

                System.out.println("observableEmitter emit data start,thread :" + Thread.currentThread().getName());
                //使用发送器发送消息
                observableEmitter.onNext("msg1");
                observableEmitter.onNext("msg2");
                observableEmitter.onNext("msg3");
                observableEmitter.onNext("msg4");
                observableEmitter.onError(new Error("404"));

            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread(),true)
                .subscribe(new Observer<String>() {
                    //订阅回调,订阅后被观察者任务才会被执行
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        /**
                         * disposable.dispose();任务会被执行，但是不会回调观察者的onNext
                         */
                        //                disposable.dispose();
                        System.out.println("=========================onSubcribe " + ",thread :" + Thread.currentThread().getName());
                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println("=========================onNext: " + s + ",thread :" + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("=========================onError: " + throwable.getMessage() + ",thread :" + Thread.currentThread().getName());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("=========================onComplete " + ",thread :" + Thread.currentThread().getName());
                    }
                });
        try {
            System.out.println("thread sleep");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("create end.....");
    }

    /**
     * just创建的observable可以支持传递多个对象，参数可以是方法（实际发送数据时是发送方法的返回值）
     * just无需订阅任务就会被执行，即无需执行observable.subscribe([observer|consumer])
     * just不能通过subscribeOn(Schedulers.newThread())指定执行的线程
     *just(T...args)
     *
     * defer接受一个java.util.concurrent.Callable<V>对象，并返回一个Observable对象
     * 该对象只有被订阅的时候，即observable.subscribe([observer|consumer])执行时才会创建observable对象，且每次都会创建一个新的observable对象
     * 该方法可以配置just使用，来延迟just执行的时机，即只有被订阅时才创建observable对象且执行任务（just）
     *Obserable<T> defer(Callable<ObservableSource<? extends T>)
     *
     *使用defer()操作符的唯一缺点就是，每次订阅都会创建一个新的Observable对象。
     * create()操作符则为每一个订阅者都使用同一个函数，所以，后者效率更高。如果有必要可以亲测性能或者尝试优化。
     *
     */
    @Test
    public void just(){

        //just无论是否被订阅都会执行，即无需subscribe(Observer observer)任务也会执行
        //final Observable observable = just(justFun());

        final Observable observable = Observable.defer(new Callable<ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> call() throws Exception {
                //just()不能通过subscribeOn(Schedulers.newThread())指定执行的线程
                try {
                    System.out.println("just emit data start,thread :" + Thread.currentThread().getName());
//                    return Observable.error(new Error("500"));
                    return Observable.just(justFun());
                } catch (Exception e) {
                    e.printStackTrace();
                    return Observable.error(e);
                }
            }
        });
        observable.subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread(),true).subscribe(new Observer<Integer>() {
                    //订阅回调,订阅后被观察者任务才会被执行
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        /**
                         * disposable.dispose();任务会被执行，但是不会回调观察者的onNext
                         */
                        //                disposable.dispose();
                        System.out.println("=========================onSubcribe " + ",thread :" + Thread.currentThread().getName());
                    }

                    @Override
                    public void onNext(Integer s) {
                        System.out.println("=========================onNext: " + s + ",thread :" + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("=========================onError: " + throwable.getMessage() + ",thread :" + Thread.currentThread().getName());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("=========================onComplete " + ",thread :" + Thread.currentThread().getName());
                    }
                });

        try {
            System.out.println("thread sleep");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("create end.....");
    }

    private int justFun(){
        try{
            int i = 1 / 0;
        }catch (Exception e){

        }
        System.out.println("justFun run....");
        return 123;
    }

}
