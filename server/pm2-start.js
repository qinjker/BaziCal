/**
 * PM2 启动文件
 * 用于生产环境启动八字历后端服务
 */

const pm2 = require('pm2');

const options = {
  name: 'bazical-server',
  script: 'dist/app.js',
  cwd: __dirname,
  instances: 1,
  exec_mode: 'fork',
  watch: false,
  max_memory_restart: '500M',
  env: {
    NODE_ENV: 'production',
    PORT: process.env.PORT || 3000
  },
  error_file: 'logs/pm2-error.log',
  out_file: 'logs/pm2-out.log',
  log_date_format: 'YYYY-MM-DD HH:mm:ss',
  merge_logs: true,
  autorestart: true,
  max_restarts: 10,
  min_uptime: '10s'
};

pm2.connect((err) => {
  if (err) {
    console.error('PM2 连接失败:', err);
    process.exit(1);
  }

  pm2.start(options, (err, apps) => {
    if (err) {
      console.error('启动失败:', err);
      pm2.disconnect();
      process.exit(1);
    }

    console.log('✅ 服务已启动');
    console.log(`📌 进程名: ${apps[0].name}`);
    console.log(`🆔 PID: ${apps[0].pid}`);

    // 显示监控命令
    console.log('\n📊 监控面板: pm2 monit');
    console.log('📝 查看日志: pm2 logs');
    console.log('🔄 重启服务: pm2 restart bazical-server');

    pm2.disconnect();
  });
});

// 处理退出信号
process.on('SIGINT', () => {
  pm2.disconnect();
  process.exit(0);
});