#!/usr/bin/zsh

args="lhs_server lhs_kt lhs_proto lhs_os lhs_push lhs_cat robot lhs_mqtt \( -path lhs_server/js -o -path lhs_kt/build -o -path robot/build -o -path robot/proto -o -path lhs_kt/proto \) -prune -o"

# c/c++
echo "# c/c++"
eval "find $args -regex '.*\.\(c\|cc\|cpp\|h\|hpp\)$' -print | xargs wc -l"

# kt/java
echo "# kt/java"
eval "find $args -regex '.*\.\(kt\|java\)$' -print | xargs wc -l"

# proto
echo "# proto"
eval "find $args -regex '.*\.\(proto\)$' -print | xargs wc -l"

# js/html
echo "# js/html"
eval "find $args -regex '.*\.\(js\|html\|xml\|json\)$' -print | xargs wc -l"

# shell/python
echo "# shell/python"
eval "find $args -regex '.*\.\(sh\|ksh\|py\)$' -print | xargs wc -l"

# md/txt
echo "# md/txt"
eval "find $args -regex '.*\.\(md\|txt\|diff\)$\|^.*/[mM]akefile$' -print | xargs wc -l"

