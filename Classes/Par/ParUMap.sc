ParUMap : ParU {

	// This class is under development. For now it plays a line between min and max.
	// it can only be used for args that have a single value ControlSpec
	// gui doesn't work yet

	/*
	example:
	x = UChain([ 'sine', [ 'freq', UMap() ] ], 'output');
	x.prepareAndStart;
	x.stop;
	*/

	classvar <>allUnits;
	classvar <>currentBus = 0, <>maxBus = 499;
	classvar >guiColor;
	classvar <>allStreams;
	classvar <>currentStreamID = 0;

	var <spec;
	var <>unitArgName;
	var <>unmappedKeys;
	var <>streamID;

	*busOffset { ^1500 }

	*guiColor { ^guiColor ?? { guiColor = Color.blue.blend( Color.white, 0.8 ).alpha_(0.4) }; }
	guiColor { ^this.class.guiColor }

	initParU { |in, inArgs, inMod, an|
		super.initParU( in, inArgs ? [], inMod, an );
		this.setunmappedKeys( inArgs );
		this.mapUnmappedArgs;
	}

	setunmappedKeys { |args|
		args = (args ? []).clump(2).flop[0];
		this.def.mappedArgs.do({ |item|
			if( args.includes( item ).not ) {
				unmappedKeys = unmappedKeys.add( item );
			};
		});
	}

	*initClass {
	    allUnits = IdentityDictionary();
	    allStreams = Order();
	}

	*defClass { ^UMapDef }

	asControlInput {
		^this.def.parGetControlInput(this)
	}

	asOSCArgEmbeddedArray { | array| ^this.asControlInput.asCollection.asOSCArgEmbeddedArray(array) }

	getBus {
		^this.def.getBus( this );
	}

	setBus { |bus = 0|
		this.def.setBus( bus, this );
	}

	nextBus {
		var res, nextBus, m;
		var buses = n.collect{
			m = this.def.numChannels;
			nextBus = currentBus + m;
			if( nextBus > (maxBus + 1) ) {
				nextBus = 0 + m;
				res = 0;
			} {
				res = currentBus;
			};
			currentBus = nextBus;
			res
		};
		^ParArg(buses);
	}

	setUMapBus {
		if( this.hasBus ) {
			this.setBus( this.nextBus );
		};
	}

	set { |...args|
		var keys;
		if( unmappedKeys.size > 0 ) {
			keys = (args ? []).clump(2).flop[0];
			keys.do({ |item|
				if( unmappedKeys.includes( item ) ) {
					unmappedKeys.remove(item);
				};
			});
		};
		^super.set( *args );
	}

	isUMap { ^true }

	hasBus { ^this.def.hasBus }

	setUMapBuses { } // this is done by the U for all (nested) UMaps

	u_waitTime { ^this.waitTime }

	dontStoreArgNames { ^[ 'u_dur', 'u_doneAction', 'u_mapbus', 'u_spec', 'u_store', 'u_prepared' ] ++ if( this.def.dontStoreValue ) { [ \value ] } { [] } }

	spec_ { |newSpec|
		if( spec.isNil ) {
			if( newSpec.notNil ) {
				spec = newSpec;
				this.mapUnmappedArgs;
			};
		} {
			if( newSpec != spec ) {
				this.def.mappedArgs.do({ |key|
					var val;
					val = this.get( key );
					if( val.isUMap.not ) {
						this.set( key, this.getSpec( key ).unmap( this.get( key ) ) );
					} {
						val.spec = nil;
					};
				});
				spec = newSpec;
				unmappedKeys = this.def.mappedArgs.copy;
				this.mapUnmappedArgs;
			}
		}
	}

	mapUnmappedArgs {
		if( spec.notNil ) {
			unmappedKeys.copy.do({ |key|
				var val;
				val = this.get( key );
				if( val.isUMap.not ) {
					this.set( key, this.getSpec( key ).map( val ) );
				} {
					val.spec = this.getSpec( key );
				};
			});
		};
	}

	// UMap is intended to use as arg for a Unit (or another UMap)
	asUnitArg { |unit, key|
		if( unit.canUseUMap( key, this.def ) ) {
			this.unitArgName = key;
			if( key.notNil ) {
				if( unit.isUMap && { unit.def.isMappedArg( key ) } ) {
					if( unit.spec.notNil ) {
						this.spec = unit.getSpec( key ).copy;
						this.set( \u_spec, [0,1,\lin].asSpec );
					};
				} {
					this.spec = unit.getSpec( key ).copy;
					this.set( \u_spec, spec );
				};
				this.def.activateUnit( this, unit );
				this.valuesAsUnitArg
			};
			^this;
		} {
			^unit.getDefault( key );
		};
	}

	unit_ { |aUnit|
		if( aUnit.notNil ) {
			case { this.unit == aUnit } {
				// do nothing
			} { allUnits[ this ].isNil } {
				allUnits[ this ] = [ aUnit, nil ];
			} {
				"Warning: unit_ \n%\nis already being used by\n%\n".postf(
					this.class,
					this.asCompileString,
					this.unit
				);
			};
		} {
			allUnits[ this ] = nil; // forget unit
		};
	}

	unit { ^allUnits[ this ] !? { allUnits[ this ][0] }; }

	unitSet { // sets this object in the unit to enforce setting of the synths
		if( this.unit.notNil ) {
			if( this.unitArgName.notNil ) {
				this.unit.set( this.unitArgName, this );
			};
		};
	}

	//needs to be changed
	getSynthArgs {
		var nonsynthKeys;
		"ParUMap getSynthArgs".postln;
		nonsynthKeys = this.argSpecs.select({ |item| item.mode == \nonsynth }).collect(_.name);
		^this.args.clump(2).select({ |item| nonsynthKeys.includes( item[0] ).not })
			.collect({ |item|
				if( this.def.isMappedArg( item[0] ) && { item[1].isUMap.not }) {
					[ item[0], this.getSpec( item[0] ) !? _.unmap( item[1] ) ? item[1] ];
				} {
					item
				};
			})
			.flatten(1);
	}

	/// UPat

	stream {
		^allStreams[ streamID ? -1 ];
	}

	stream_ { |stream|
		this.makeStreamID;
		allStreams[ streamID ] = stream;
	}

	*nextStreamID {
		^currentStreamID = currentStreamID + 1;
	}

	makeStreamID { |replaceCurrent = false|
		if( replaceCurrent or: { streamID.isNil }) {
			streamID = this.class.nextStreamID;
		};
	}

	makeStream {
		this.def.makeStream( this );
	}

	resetStream {
		this.stream.reset;
	}

	next { ^this.asControlInput }

	disposeFor {
		if( this.unit.notNil && { this.unit.synths.select(_.isKindOf( Synth ) ).size == 0 }) {
			this.unit = nil;
		};
		if( this.def.isKindOf( FuncUMapDef ) ) {
			this.values.do{ |val|
	       	 if(val.respondsTo(\disposeFor)) {
		            val.disposeFor( *args );
		        }
		    };
		};
	}

	dispose {
	    this.free;
	    this.values.do{ |val|
	        if(val.respondsTo(\dispose)) {
	            val.dispose
	        }
	    };
	    this.modPerform( \dispose );
	    preparedServers = [];
	    this.unit = nil;
	}

	startMod { |startPos|
		this.modPerform( \start, startPos );
	}

}

ImmUMap : ParUMap {

	var <>surface;

	*new { |surfaceKey, def, args, mod|
		^super.prNewBasic().initImmUMap(surfaceKey, def, args, mod)
	}

	initImmUMap { |asurface, in, inArgs, inMod|
		//surface = PSurfaceDef.get(surfaceKey); //only thing different
		surface = asurface;
		n = surface.size; //only thing different
		if( in.isKindOf( this.class.defClass ) ) {
			def = in;
			defName = in.name;
			if( defName.notNil && { defName.asUdef( this.class.defClass ) == def } ) {
				def = nil;
			};
		} {
			defName = in.asSymbol;
			def = nil;
		};
		if( this.def.notNil ) {
			inArgs.pairsDo{ |key, val|
				val.parMatch({},{|x| (x.size != n).assert("ParArg size % / ParU size % : size mismatch - %".format(x.size,n,val)) });
			};
			args = this.def.asArgsArrayPar( inArgs ? [], this );
		} {
			args = inArgs;
			"def '%' not found".format(in).warn;
		};
		preparedServers = [];
		mod = inMod.asUModFor( this );
		this.changed( \init );
	}

}

+ UMapDef {

	parGetControlInput { |unit|
		^if( this.hasBus ) {
			this.getBus(unit).parCollect{ |bus|
				if( this.numChannels > 1 ) {
					this.numChannels.collect({ |i|
						("c" ++ (bus + i + unit.class.busOffset)).asSymbol;
					});
				} {
					("c" ++ (bus + unit.class.busOffset)).asSymbol;
				}
			}
		} {
			this.value( unit ).asControlInput;
		}
	}

	asArgsArrayPar { |argPairs, unit, constrain = true|
		argPairs = argPairs ? #[];
		^argSpecs.collect({ |item|
			var val;
			val = argPairs.pairsAt(item.name) ?? { item.default.copy };
			val = val.parCollect{ |x| x.deepCopy.asUnitArgPar( unit, item.name ) };
			if( constrain && this.isMappedArg( item.name ).not && { (val.isKindOf( UMap ) || val.isKindOf( ParUMap ) ).not } ) {
				val = val.parCollect{ |x| item.constrain( x ) }
			};
			[ item.name,  val ]
		}).flatten(1);
	}

	setSynthPar { |unit ...keyValuePairs| //keyValuePairs :: [ keyValueArrays ]n
		[U.synthDict[unit], keyValuePairs].flopWith{ |synths, keyValuePairs2|
			keyValuePairs2 = keyValuePairs2.clump(2).collect({ |item|
				if( this.isMappedArg( item[0] ) && { item[1].isUMap.not } ) {
					"mapped value %".format(item[0]).postln;
					[ item[0], this.getSpec( item[0], unit ) !? _.unmap( item[1] ) ? item[1] ].postln;
				} {
					item
				};
			}).flatten(1);
			this.prSetSynth( synths, *keyValuePairs2 )
		};
	}

}

