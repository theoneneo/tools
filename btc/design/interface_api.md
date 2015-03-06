Android程序与运营系统之间的数据交互
# 1. Android定期向运营系统POST数据

	间隔：5 min

#### CPU 使用率
	POST	http://ip/cpu_info
	Content
		{
		    "atm_name" : atm的public key
		    "time" : atm的时间戳（格式yyyy-MM-dd HH:mm:ss）
		    "cpu_usage" : CPU使用率（6.32 for a CPU usage of 6.32% or -1 if the value is not available.）
		    "time_period" : 统计周期(秒)
		}
	Result
		'OK'/'Fail'


#### 内存使用率
	POST	http://ip/memory_info
	Content
		{
		    "atm_name" : atm的public key
		    "time" : atm的时间戳（格式yyyy-MM-dd HH:mm:ss）
		    "memory_usage" : 内存使用率（30.25 for a memory usage of 30.25% or -1 if the value is not available.）
		    "time_period" : 统计周期(秒)
		}
	Result
		'OK'/'Fail'

#### 网络吞吐
需要考虑有线网和无线网两个接口的流量

	POST	http://ip/network_info
	Content
		{
		    "atm_name" : atm的public key
		    "time" : atm的时间戳（格式yyyy-MM-dd HH:mm:ss）
		    "interface" : 接口名称（android /dev文件夹中的设备文件名 如wlan0、eth0等）
		    "throughput_tx" : 发送方向网络吞吐(MB/s)
		    "throughput_rx" : 接收方向网络吞吐(MB/s)
		    "time_period" : 统计周期(秒)
		}
	Result
		'OK'/'Fail'

#### 交易数据
每发生一笔交易的时候，向运营系统发送

	POST	http://ip/transaction
	Content
		{
		    "atm_name" : atm地址（公钥）
		    "payer_addr" : 付款者地址（公钥）
		    "time" : atm的时间戳（格式yyyy-MM-dd HH:mm:ss）
	    	"direction" : “buy_coins”或“sell_coins”
	    	"btc_amount" : 比特币数额
            "cash_amount" : 现金数额
    		"exchange_rate" : 法币：比特币的汇率（1BTC对应的法币数额），注意：是没有乘以系数的汇率
    		"handling_charge_proportion" : 商家设定的系数，例如1.05，为商家收取5%的交易费
    		"trade_status" : 交易状态，用于指示本次交易成功和失败及其原因 0:成功 1:失败
		}
	Result
		'OK'/'Fail'


# 2. Android定期更新运营系统的数据

	间隔：15 s

#### 更新系统状态参数（以下JSON中各参数值为默认参数值）
	GET		http://ip/setting_info/<ATM公钥>
	Content
		{
			"default_timeout": 15, // 默认超时时间
			"bitcoin_trading_limit": 100, // 当日交易限额，以比特币为单位，精确到小数点后6位
			"cash_out_limit": 5000, // 最高自动出钞额度，以法币为单位，精确到小数点后2位
			"balance_warning_threshold" : 3, // 余额告警阈值，以比特币为单位，精确到小数点后6位
			"exchange_rate_api_url" : "https://www.okcoin.cn/api/ticker.do,https://blockchain.info/ticker", // 汇率接口API地址
			"sms_platform_url" : "http://utf8.sms.webchinese.cn/?Uid=btcatm&Key=dae55ec097981a932a87&smsMob=", // 短信通知平台URL
			"sms_mobile": "18611147179,18611121112",
			"kyc_enable" : false, // true: 启动KYC， false: 关闭KYC
			"handling_charge_proportion" : 1.05, // 手续费比例
			"quick_payment_cash_threshold" : 100, // 快速支付现金阈值
			"network_interface" : "wlan0", // 网络类型
			"cpu_info_post_period_sec" : 300, // 上传CPU_INFO周期
			"memory_info_post_period_sec" : 300, // 上传MEMORY_INFO周期
			"network_info_post_period_sec" : 300, // 上传NETWORK_INFO周期
			"setting_info_get_period_sec" : 15, // 获取SETTING_INFO周期
			"ui_info_get_period_sec" : 15 // 获取UI_INFO周期
		}


有关短信通知平台URL的格式的说明，以如下URL为例：

	http://utf8.sms.webchinese.cn/?Uid=本站用户名&Key=接口安全密码&smsMob=手机号码&smsText=短信内容

考虑到客户的使用体验，短信内容应该可定制。短信内容模版中有以下参数可以由程序自动插入，例如采用以下模版：

对于卖出比特币交易：

	贵平台<atm_name>于<time>发生一笔卖出比特币交易，交易额为卖出比特币<btc_amount>，兑换人民币<cash_amount>，即时汇率为<exchange_rate>，手续费率为<handling_charge_proportion>。
	其中：
		atm_public_key:					ATM机的公钥
		time:  							交易发生的时间，格式为：2014-06-01 12:00:00
		btc_amount:						交易总额，以比特币为单位，精确到小数点后6位
		cash_amount:					交易总额，以现金为单位
		exchange_rate:					交易汇率，法币：比特币，精确到小数点后2位
		handling_charge_proportion:		手续费率，商家设定的系数，例如1.05，为商家收取5%的交易费

	在运营平台设置的格式应为：
	sms_platform_url应该设置为：
		http://utf8.sms.webchinese.cn/?Uid=hxgqh&Key=hxgqh&smsMob=18611147179&smsText=贵平台<atm_name>于<time>发生一笔卖出比特币交易，交易额为卖出比特币<btc_amount>，兑换人民币<cash_amount>，即时汇率为<exchange_rate>，手续费率为<handling_charge_proportion>。

对于买入比特币交易同理。


#### 更新语言（以下JSON中各参数值为默认参数值）

	GET http://ip/ui_info/<ATM公钥>
	Content
		{
			"tag": '0cc175b9c0f1b6a831c399e269772661', // 只要下面字段内容与上次更新发生变化，TAG需要与上次的不同
			"common.prv_btn" : "上一步",
			"common.nxt_btn" : "下一步",
			"common.cfm_btn" : "确认",
			"common.cnl_btn" : "取消",
			"common.fns_btn" : "完成",
			"common.btc_abb" : "BTC",
			"common.cash_abb" : "RMB",
			"common.rate_abb" : "RMB/BTC",
			"common.timeout_title" : "本页面剩余操作时间：",
			"common.tradetype_cash" : "卖出比特币",
			"welcomepage.title" : "欢迎使用\n比特币自动售卖机",
			"welcomepage.hint" : "点击屏幕，开始交易",
			"trademode.title" : "请选择交易模式",
			"trademode.bitcoin_btn" : "购买比特币",
			"trademode.cash_btn" : "卖出比特币",
			"trademode.qr_btn" : "扫描提款二维码",
			"cashtrade.exchangerate.title" : "当前汇率",
			"cashtrade.exchangerate.hint" : "汇率更新完毕后，点击“下一步”继续交易",
			"cashtrade.input.title" : "请输入卖出比特币数额",
			"cashtrade.input.hint" : "本机仅支持100元面额现金兑换",
			"cashtrade.input.exceed" : "超出交易限额，请修改输入金额",
			"cashtrade.input.overflow" : "本机货币不足，请修改输入金额",
			"cashtrade.input.valid" : "数额验证通过，请点击下一步继续交易",
			"cashtrade.requestcoin.title" : "支付比特币",
			"cashtrade.requestcoin.hint" : "请扫描二维码地址转账",
			"cashtrade.requestcoin.btc_title" : "支付比特币：",
			"cashtrade.requestcoin.cash_title" : "兑换现金：",
			"cashtrade.checkpayment.title" : "系统正在确认您的支付",
			"cashtrade.checkpayment.hint" : "请稍等",
			"cashtrade.checkpaymenttimeout.title" : "未收到您的转账交易广播，转账失败",
			"cashtrade.checkpaymenttimeout.hint" : "请联系客服:010-88888888",
			"cashtrade.showcashqr.title" : "提款信息",
			"cashtrade.showcashqr.hint1" : "请妥善保管提款二维码",
			"cashtrade.showcashqr.hint2" : "请于约20分钟后凭二维码提取现金，您的等待对于我们很重要",
			"cashtrade.showcashqr.btc_title" : "支付比特币：",
			"cashtrade.showcashqr.cash_title" : "兑换现金：",
			"cashtrade.showscanqr.title" : "验证提款二维码",
			"cashtrade.showscanqr.hint_default" : "请等待",
			"cashtrade.showscanqr.hint_unconfirmed" : "您的交易尚未确认完毕，请耐心等待",
			"cashtrade.showscanqr.hint_unknown" : "二维码错误，交易不存在",
			"cashtrade.showscanqr.hint_forbidden" : "交易状态非法，禁止交易",
			"cashtrade.showscanqr.rescan_btn" : "重新扫描",
			"cashtrade.checkingout.title" : "交易明细",
			"cashtrade.checkingout.hint" : "请点击“提款”按钮提取现金",
			"cashtrade.checkingout.btc_title" : "卖出比特币：",
			"cashtrade.checkingout.cash_title" : "兑换现金：",
			"cashtrade.checkingout.rate_title" : "交易汇率：",
			"cashtrade.checkingout.checkout" : "提款",
			"cashtrade.cashout.title" : "正在出钞",
			"cashtrade.success.title" : "交易成功",
			"cashtrade.success.btc_title" : "成功卖出：",
			"cashtrade.success.cash_title" : "成功兑换：",
			"cashtrade.success.rate_title" : "交易汇率：",
			"cashtrade.success.sms_template" : "贵平台<atm_name>于<time>发生一笔卖出比特币交易，交易额为卖出比特币<btc_amount>，兑换人民币<cash_amount>，即时汇率为<exchange_rate>，手续费率为<handling_charge_proportion>。"  // 这里需要保证<>中内容不变，唯一且不缺失
			"cashtrade.failure.title" : "交易失败",
			"cashtrade.failure.hint" : "请取走交易失败凭条，并联系客服:010-88888888",
			"cashtrade.kyclogin.newuser_title" : "首次使用\n请点击“注册”按钮",
			"cashtrade.kyclogin.newuser_hint" : "提示：根据法律要求，新用户注册需扫描ID卡或护照等带有照片的身份证件",
			"cashtrade.kyclogin.title" : "KYC登录",
			"cashtrade.kyclogin.phone_title" : "手机号：",
			"cashtrade.kyclogin.sendmsg_btn" : "获取验证码",
			"cashtrade.kyclogin.veri_title" : "验证码：",
			"cashtrade.kyclogin.reg_btn" : "注册",
			"cashtrade.kyclogin.login_btn" : "登录",
			"cashtrade.kycregister.step" : "用户注册步骤：",
    		"cashtrade.kycregister.step1" : "Step1：头像采集",
    		"cashtrade.kycregister.step2" : "Step2：证件扫描",
    		"cashtrade.kycregister.step3" : "Step3：手机号码注册",
    		"cashtrade.kycregister1.title" : "Step1：请面向摄像头采集您的头像",
    		"cashtrade.kycregister1.rescan_btn" : "重拍",
    		"cashtrade.kycregister2.title" : "Step2：请扫描您的证件",
    		"cashtrade.kycregister2.rescan_btn" : "重拍",
    		"cashtrade.kycregister3.title" : "Step3：请注册您的手机号码",
    		"cashtrade.kycregister3.phone_title" : "手机号：",
    		"cashtrade.kycregister3.sendmsg_btn" : "获取验证码",
    		"cashtrade.kycregister3.veri_title" : "验证码：",
    		"cashtrade.kycregister3.login_btn" : "登录",
			"cashtrade.kycsuccess.title" : "您已通过KYC验证",
			"cashtrade.kycsuccess.hint" : "您的个人信息如下：",
			"cashtrade.kycsuccess.phone_title" : "姓名：",
			"cashtrade.kycfailure.title" : "KYC验证失败",
			"cashtrade.kycfailure.retry_prefix" : "本日您还有",
			"cashtrade.kycfailure.retry_suffix" : "次尝试机会",
			"cashtrade.kycfailure.retry_btn" : "重新登录",
			"cashtrade.pause.title" : "对不起，本机暂停交易"
		}

#### 更新系统是否交易命令
	GET		http://ip/trade_cmd/<ATM公钥>
	Content
		{
			"sw_shutdown" : flase, // false: 允许交易开始， true：交易停止
			"trade_cmd_get_period_sec" : 15 // 获取系统是否交易命令周期
		}