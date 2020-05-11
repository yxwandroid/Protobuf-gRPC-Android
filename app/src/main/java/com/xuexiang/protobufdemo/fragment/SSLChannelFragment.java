package com.xuexiang.protobufdemo.fragment;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xuexiang.protobufdemo.GreeterGrpc;
import com.xuexiang.protobufdemo.HelloReply;
import com.xuexiang.protobufdemo.HelloRequest;
import com.xuexiang.protobufdemo.R;
import com.xuexiang.protobufdemo.grpc.SimpleStreamObserver;
import com.xuexiang.protobufdemo.grpc.gRPCChannelPool;
import com.xuexiang.protobufdemo.grpc.gRPCChannelUtils;
import com.xuexiang.xaop.annotation.MainThread;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.base.XPageFragment;
import com.xuexiang.xutil.common.StringUtils;
import com.xuexiang.xutil.tip.ToastUtils;

import butterknife.BindView;
import butterknife.OnClick;
import io.grpc.ManagedChannel;

/**
 * @author xuexiang
 * @since 2019/3/24 下午1:31
 */
@Page(name = "gRPC-Https请求")
public class SSLChannelFragment extends XPageFragment {

    @BindView(R.id.et_host)
    EditText etHost;
    @BindView(R.id.et_port)
    EditText etPort;
    @BindView(R.id.et_message)
    EditText etMessage;
    @BindView(R.id.tv_grpc_response)
    TextView tvGrpcResponse;
    @BindView(R.id.btn_send)
    Button btnSend;

    GreeterGrpc.GreeterStub mStub;
    /**
     * 布局的资源id
     *
     * @return
     */
    @Override
    protected int getLayoutId() {
        return R.layout.layout_grpc_test;
    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {
        tvGrpcResponse.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    /**
     * 初始化监听
     */
    @Override
    protected void initListeners() {

    }

    @SingleClick
    @OnClick(R.id.btn_send)
    public void onViewClicked(View view) {
        String host = etHost.getText().toString();
        int port = StringUtils.toInt(etPort.getText().toString(), 50051);
        String message = etMessage.getText().toString();

        if (StringUtils.isEmpty(host)) {
            ToastUtils.toast("服务地址不能为空");
            return;
        }

        if (StringUtils.isEmpty(message)) {
            ToastUtils.toast("内容不能为空");
            return;
        }

        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(etHost.getWindowToken(), 0);
        btnSend.setEnabled(false);
        tvGrpcResponse.setText("");

        //开始网络请求
        //构建通道,设置CA证书
//         final ManagedChannel channel = gRPCChannelUtils.newSSLChannel(host, port, authority, ca证书);
        final ManagedChannel channel = gRPCChannelUtils.newChannel(host, port);
        //构建服务api代理
        mStub = GreeterGrpc.newStub(channel);
        //构建请求实体
        HelloRequest request = HelloRequest.newBuilder().setName(message).build();
        //进行请求
        mStub.sayHello(request, new SimpleStreamObserver<HelloReply>() {
            @Override
            protected void onSuccess(HelloReply value) {
                tvGrpcResponse.setText(value.getMessage());
                btnSend.setEnabled(true);
            }

            @MainThread
            @Override
            public void onError(Throwable t) {
                super.onError(t);
                tvGrpcResponse.setText(Log.getStackTraceString(t));
                btnSend.setEnabled(true);
            }

            @Override
            public void onCompleted() {
                super.onCompleted();
                gRPCChannelUtils.shutdown(channel);
            }
        });
    }
}
