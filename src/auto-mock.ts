const mockData = () => {
  if(process.env.NODE_ENV !== 'development') {
    return
  }
  // @ts-ignore
  const mockFiles = require.context('./mock', true, /\.ts$/);
  mockFiles.keys().forEach((key: any) => {
    mockFiles(key);
    // 处理 mockModule
  });
}
mockData()
export default mockData