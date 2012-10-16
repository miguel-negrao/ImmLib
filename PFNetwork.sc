//to delete
PFNetwork {

	var <netDesc;

	*new{ arg func ...args;
		var timer = EventNetwork.newTimer.asWriterReader;
		var f = { |theargs, bindargs|
			theargs.match({
				func.(*bindargs.asArray).asWriterReader
			},{ |onearg,rest|
				onearg >>= { |bindvar| f.(rest, bindargs.add(bindvar) ) }
			})
		};
		^super.newCopyArgs( f.( timer %% LazyList.fromArray(args.collect(_.asWriterReader) ), LazyListEmpty ) );
	}


}

//to delete
PFieldOld1 {
	var <func;

	*new{ |f| // f = { |p,t, c1, c2, ...| ...}
		^super.newCopyArgs(f)
	}

	value{ |...args|
		^WriterReader( { |points|
			var f;
			" points are %".format(points).postln;
			f = { |args2| points.collect{ |p| func.(*([p]++args2)) } };
			Tuple2( f <%> args.sequence, Tuple2([],[]) );
		} )
	}
}

//to delete
PFieldOld2 {
	var <func;

	*new{ |f| // f = { |p,t, c1, c2, ...| ...}
		^super.newCopyArgs(f)
	}

	value{ |...args|
		var f = { |args2| Reader( { |points| points.collect{ |p| func.(*([p]++args2)) } } ) };
		^f <%> args.sequence
	}
}

//to delete
PFNetwork3 {

	*new{ arg func ...args;
		var argsEN = args.collect(_.asENInput).asLazy;
		var timer = EventNetwork.newTimer.collect{ |x| x.hold(0.0) };
		var f = { |theargs, bindargs|
			theargs.match({
				func.(*bindargs.asArray)
			},{ |onearg,rest|
				onearg >>= { |bindvar| f.(rest, bindargs.add(bindvar) ) }
			})
		};
		var desc = f.( timer %% argsEN, LazyListEmpty );
		//var desc = func <%%> (timer %% args.asLazy);
		^EventNetwork( desc );
	}


}
//just temporary hack, to be deleted
PFNetwork4 {

	*new{ arg func,delta=0.1 ...args;
		var argsEN = args.collect(_.asENInput).asLazy;
        var timer = EventNetwork.newTimer(delta).collect{ |x| x.hold(0.0) };
		var f = { |theargs, bindargs|
			theargs.match({
				func.(*bindargs.asArray)
			},{ |onearg,rest|
				onearg >>= { |bindvar| f.(rest, bindargs.add(bindvar) ) }
			})
		};
		var desc = f.( timer %% argsEN, LazyListEmpty );
		//var desc = func <%%> (timer %% args.asLazy);
		^EventNetwork( desc );
	}


}

PField {
	var <func;

	*new{ |f|  // f = { |p,t, c1, c2, ...| ...}

		^super.newCopyArgs(f)
	}

	value{ |surface...args| //args should all be FPSignals or EventStreams
		var f = { |args2|
			surface.points.collect{ |p| func.(*([p]++args2)) }
		};
		^f <%> args.sequence
	}
}

PFieldDef {
    classvar <all;
	var <key, <func;

	*new{ |key, f|  // f = { |p,t, c1, c2, ...| ...}

		var obj = super.newCopyArgs(key, f);
		if(all.isNil) { all = IdentityDictionary.new };
		all.put(key, obj);
		^obj
	}

	*value{ |key,surface...args| //args should all be FPSignals or EventStreams
		var f = { |args2|
            surface.points.collect{ |p|
				all.at(key) !? { |pfdef| pfdef.func.(*([p]++args2)) } ?? { 0 }
			}
		};
		^f <%> args.sequence;
	}
}


