
//prepare and dispose must happen n times
//this is because for instance DiskSndFile must be loaded once per synth since it can't be re-used by synths
ParU : U {
	classvar <>currentIndex;
	var <n;
	var <serversDisposedFor;

	//synthDict[ this ] :: Dict ParU [ [synth]m ]n

	*prNewBasic{ ^super.new }

	*new { |n, def, args, mod|
		this.checkArgs("ParU","new",[n],[Integer]);
		^super.new().initParU(def, args, mod, n)
	}

	initParU { |in, inArgs, inMod, an|
		n = an;
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

	//disable U's init
	init { }

	def_ { |newDef, keepArgs = true|
        this.initParU( newDef, if( keepArgs ) { args } { [] }, mod, n); // keep args
    }

	synths { ^synthDict[ this ] !? _.flatten ? [] }

	addSynth { |synth|
		this.addSynthPar( ParU.currentIndex, synth)
	}

	removeSynth { |synth|
		//"removeSynth synths are %".format(synthDict[this]).postln;
		//try to remove in all sub collections
		n.do{ |i|
			this.removeSynthPar( i, synth)
		};
		if( synthDict[this].every{ |x| x.size == 0 } ) {
			synthDict.put( this, nil );
		};
	}

	addSynthPar { |i, synth|
		if(synthDict[this].isNil){ synthDict.put(this, [].dup(n) ) };
		synthDict[this][i] = synthDict[this][i].add( synth );
	}

	removeSynthPar { |i, synth|
		//var t = " removeSynthPar synths are %".format(synthDict[this]).postln;
		var synths = synthDict[this][i];
		synths.remove( synth );
		synthDict[this][i] = synths;
	}

	*formatArgs { |inArgs, server, startPos = 0|
		//"ParU formatArgs".postln;
		^inArgs.clump(2).collect({ |item, i|
			[ item[0], switch( item[0],
				\u_startPos, { startPos },
				\u_dur, { item[1] - startPos },
				\u_fadeIn, {
					if( startPos > 0 ) {
						(item[1] - startPos).max(0.025)
					} {
						item[1]
					};
				},
				{
					if( item[1].isKindOf(ParUMap) )  {
						//can return a ParArg with bus mapping symbols or a normal value
						item[1].asControlInput.parMatch(I.d, _[ParU.currentIndex])
					}{
						item[1].parMatch(
							_.asControlInputFor( server, startPos ),
							{ |x| x[ParU.currentIndex].asControlInputFor( server, startPos ) }
						)
					}
				}
			) ];
		}).flatten(1);
	}

	makeSynth { |target, startPos = 0, synthAction|
		var synth;
		synth = this.def.makeSynthPar( this, target, startPos, synthAction );
		if( synth.notNil ) {
			this.umapPerform( \makeSynth, synth, startPos );
			this.umapPerform( \startMod, startPos );
		};
	}

	// target :: [ [node]m ]n
	makeBundle { |targets, startPos = 0, synthAction|
		^targets.collect({ |innertargets, i|
			ParU.currentIndex = i;
			innertargets.collect({ |target|
				target.asTarget.server.makeBundle( false, {
					this.makeSynth(target, startPos, synthAction)
				});
			})
		})
	}

	// target :: [ [node](m - parallel servers) ] (n - paru multiplier)
	prepare { |target, startPos = 0, action|
		var valuesToPrepare, valuesToPreparePar, valuesToPrepareSingle, act, servers, flatServers;
		//"entered ParU prepare %".format(this).postln;
		serversDisposedFor = [];
		parentChain = UChain.nowPreparingChain;

		target = target.collect{ |target| target.asCollection.collect{ |t| t.asTarget( this.apxCPU ) } };
		target = target.collect{ |target| target.select({ |tg|
			this.shouldPlayOn( tg ) != false;
		}) };
		servers = target.collect{ |target| target.collect(_.server) };
		flatServers = servers.flat.as(Set).as(Array);

		if( flatServers.size > 0 ) {
			act = MultiActionFunc({
				preparedServers = preparedServers.addAll( flatServers );
				"done ParU prepare %".format(this);
				action.value
			});
			if( loadDef) {
				this.def.loadSynthDef( flatServers );
			};
			this.valuesSetUnit;
			valuesToPrepare = this.valuesToPrepare;

			//divide in par and single
			valuesToPrepareSingle = valuesToPrepare.select({ |x| x.isKindOf(ParArg).not });
			valuesToPreparePar = valuesToPrepare.select(_.isKindOf(ParArg));


			if( valuesToPrepare.size > 0  ) {
				//prepare just for each server
				valuesToPrepareSingle.do{ |val|
					"single arg preparing % %".format(val, flatServers);
					val.prepare(flatServers, startPos, act.getAction, this)
				};
				//prepare n times
				valuesToPreparePar.do{ |val|
					[servers, val.array].flopWith{ |servers,val|
						"pararg preparing % %".format(val, flatServers);
						val.prepare(servers, startPos, act.getAction, this)
					}
				};
				this.def.prepare(flatServers, this, act.getAction)
			} {
				this.def.prepare(flatServers, this, act.getAction);
			};
		} {
			action.value;
		};
		this.modPerform( \prepare, startPos );
		this.setUMapBuses;
		^target; // returns targets actually prepared for
	}

	disposeArgsFor { |server, i|
		"ParU - disposeArgsFor % % %".format(this, server, i);

		this.values.do{ |val|
			val.parMatch({
				if(serversDisposedFor.includes(server).not and: {val.respondsTo(\disposeFor)} ) {
					"ParU - disposeArgsFor single % % % %".format(this, server, i, val);
					val.disposeFor(server, this)
				}
			},{
				if(val.array[i].respondsTo(\disposeFor)) {
					"ParU - disposeArgsFor par % % % %".format(this, server, i, val);
					val.array[i].disposeFor(server, this)
				}
			})
		};
		this.modPerform( \dispose );
		preparedServers.remove( server );
		if( preparedServers.size == 0 ) {
			parentChain = nil; // forget chain after disposing last server
		};
		serversDisposedFor = serversDisposedFor.add(server);
	}

	// target :: [ [node]m ]n
	start { |target, startPos = 0, latency|
		var targets, bundles;
		targets = target ? preparedServers ?  [Server.default].dup(n);
		latency = latency ? 0.2;
		this.modPerform( \start, startPos, latency );
		bundles = this.makeBundle( targets, startPos ); //:: [ [bundle]m ]n
		[targets, bundles].flopWith{| innerT, innerB|
			[innerT, innerB].flopWith{| target, bundle|
				if( bundle.size > 0 ) {
					target.asTarget.server.sendSyncedBundle( latency, nil, *bundle );
				};
			}
		};
		^synthDict[this]
	}

	set { |...args|
		var synthArgs, synthArgsNormal, synthArgsPar;
		args.pairsDo({ |key, value|
			var ext, extid;
			ext = key.asString;
			extid = ext.find( "." );
			if( extid.notNil ) {
				key = ext[..extid-1].asSymbol;
				ext = ext[extid+1..];
				if( ext[0].isDecDigit ) {
					value = this.get( key ).put( ext.asInteger, value );
				} {
					value = this.get( key ).perform( ext.asSymbol.asSetter, value );
				};
			} {
				value = value.parCollect(_.asUnitArg( this, key ));
			};
			this.setArg( key, value );
			synthArgs = synthArgs.add( value.asParArg(n).array.collect{ |v| [key,v] });
		});

		this.def.setSynthPar( this, *synthArgs.flop.collect(_.flatten) )
	}

	mapSet { |...args|
		var argsWithSpecs = args.clump(2).collect{ |arr|
			var key, value, spec;
			#key, value = arr;
			spec = this.getSpec(key);
			if( spec.notNil ) {
				[key, value.parCollect( spec.map(_) ) ]
			} {
				[key, value ]
			}
		};
		this.set( * argsWithSpecs.flatten )
	}

	mapGet { |key|
		var spec = this.getSpec(key);
		^if( spec.notNil ) {
			this.get(key).parCollect( spec.unmap(_) )
		} {
			this.get(key)
		}
	}

	asParUnit{ ^this }

	gui { |parent, bounds| ^ParUGUI( parent, bounds, this ) }

	storeArgs {
		var initArgs, initDef;
		initArgs = this.getInitArgs;
		initDef = if( this.def.class.callByName ) {
		    this.defName
		} {
		    this.def
		};
		if( mod.notNil ) {
			^[ n, initDef, initArgs, mod ];
		} {
			if( (initArgs.size > 0) ) {
				^[ n, initDef, initArgs ];
			} {
				^[ n, initDef ];
			};
		};
	}

	storeArgsWithoutN { ^super.storeArgs }

	getInitArgs {
		var defArgs;
		defArgs = (this.def.args( this ) ? []).clump(2);
		^args.clump(2).select({ |item, i|
			(item != defArgs[i]) && { this.dontStoreArgNames.includes( item[0] ).not };
		 }).collect({ |item|
			 var umapArgs;
			 if( item[1].isUMap ) {
				 umapArgs = item[1].storeArgsWithoutN;
				 if( umapArgs.size == 1 ) {
				 	[ item[0], umapArgs[0] ]
				 } {
					 [ item[0], umapArgs ]
				 };
			 } {
				 item
			 };
		 }).flatten(1);
	}

	insertUMap { |key, umapdef, args|
		var item;
		umapdef = umapdef.asUdef( UMapDef );
		if( umapdef.notNil ) {
			if( umapdef.canInsert ) {
				item = this.get( key );
				this.set( key, ParUMap(n, umapdef,  args ) );
				this.get( key ).setConstrain( umapdef.insertArgName, item );
			} {
				this.set( key, ParUMap(n, umapdef, args ) );
			};
		};
	}

	removeUMap { |key|
		var umap;
		umap = this.get( key );
		if( umap.isKindOf( ParUMap ) ) {
			if( umap.def.canInsert ) {
				this.set( key, umap.get( umap.def.insertArgName ) );
			} {
				this.set( key, this.getDefault( key ) );
			};
		};
	}

}

+ Udef {

	makeSynthPar { |unit, target, startPos = 0, synthAction|
		var synth;
		var started = false;
		var index = ParU.currentIndex;
		if( unit.shouldPlayOn( target ) != false ) {
			/* // maybe we don't need this, or only at verbose level
			if( unit.preparedServers.includes( target.asTarget.server ).not ) {
			"U:makeSynth - server % may not (yet) be prepared for unit %"
			.format( target.asTarget.server, this.name )
			.warn;
			};
			*/
			synth = this.createSynth( unit, target, startPos );
			synth.startAction_({ |synth|
				unit.changed( \go, synth );
				started = true;
			});
			synth.freeAction_({ |synth|
				//"running freeAction_ %".format(synth).postln;
				if( started == false ) { synth.changed( \n_go ) };
				unit.removeSynth( synth );
				synth.server.loadBalancerAddLoad( this.apxCPU.neg );
				unit.changed( \end, synth );
				if(unit.disposeOnFree) {
					unit.disposeArgsFor(synth.server, index)
				}
			});
			unit.changed( \start, synth );
			synthAction.value( synth );
			unit.addSynth(synth);
		};
		^synth;
	}

}