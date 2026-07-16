if (config.devServer) {
  config.devServer.historyApiFallback = {
      rewrites: [
          { from: /^\/app-ads\.txt$/, to: '/app-ads.txt' },
          { from: /^\/[^.]+$/, to: '/index.html' },
      ],
  };

  config.output = {
      ...config.output,
      publicPath: '/',  // Serve all static assets from the root, regardless of the current route
  };
}
