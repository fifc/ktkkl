cd /opt
old_version=`rustc --version`
curl -O -C - https://static.rust-lang.org/dist/rust-nightly-x86_64-unknown-linux-gnu.tar.gz || exit
tar zxf rust-nightly-x86_64-unknown-linux-gnu.tar.gz;rm rust-nightly-x86_64-unknown-linux-gnu.tar.gz
cd rust-nightly-x86_64-unknown-linux-gnu
./install.sh --prefix=/opt
cd ..
rm -rf rust-nightly-x86_64-unknown-linux-gnu
new_version=`rustc --version`
echo "${old_version} => ${new_version}"
