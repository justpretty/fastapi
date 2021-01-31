curl https://t1.frontlinemedical.cn/adminconfig/debug/false
cat >post.txt<<EOF

EOF
mkdir /downloads
cd /downloads
wget http://download.joedog.org/siege/siege-latest.tar.gz
tar zxf siege-latest.tar.gz
cd siege-*
./configure
make && make install

siege -T 'application/json' -c 500 -r 200 -d 1 'https://t1.frontlinemedical.cn/api/selec POST <./post.txt'
