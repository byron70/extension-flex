rm -fr dist
rm -fr tmp
docker run --rm -it -v $PWD:/var/current/app:rw --workdir /var/current/app frekele/ant:1.10.3-jdk8 ant
mkdir -p tmp
unzip ./dist/lucee-flex-*.zip -d ./tmp/
rm -f /d/lucee-1/lib/ext/lucee-flex*.jar
cp -f ./tmp/jars/*.jar /d/lucee-1/lib/ext/
