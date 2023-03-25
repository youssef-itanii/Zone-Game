all: ${SRC} 
	ant -buildfile build.xml build

# make run f="*.ml"
run:
	./mincamlc ../mincaml/$(f)


clear_reg:
 	kill $(lsof -t -i:1099)

clean:
	ant -buildfile build.xml clean
