if (config.devServer) {
  config.devServer.historyApiFallback = {
      rewrites: [
          { from: /^\/[^.]+$/, to: '/index.html' }
      ],
  };

  config.output = {
      ...config.output,
      publicPath: '/',  // Serve all static assets from the root, regardless of the current route
  };
}
