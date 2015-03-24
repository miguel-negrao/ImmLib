ParUChain : UChain {
	classvar <>prepareCurrentIndex;
	var <n;
	var <>private = false;

	*prBasicNew {
		^super.new
	}

	*new { |n...args|
		this.checkArgs("ParUChain","new",[n],[Integer]);
		^super.new().initParUChain(n, args);
	}

	initParUChain { |an, args|

		var tempDur;

		n = an;

		if( args[0].isNumber ) {
			startTime = args[0];
			args = args[1..]
		};
		if( args[0].isNumber ) {
			track = args[0];
			args = args[1..]
		};
		if( args[0].isNumber ) {
			tempDur = args[0];
			args = args[1..]
		};
		if( args[0].class.superclass == Boolean ) {
			releaseSelf = args[0]; args = args[1..]
		};

		units = args.collect(_.asParUnit(n));
		if( tempDur.notNil ) { this.duration = tempDur };

		prepareTasks = [];

		units.reverse.do(_.uchainInit( this ));

		this.changed( \init );
	}

	init {}

	defaultTarget {
		^(ULib.servers ? Server.default).asCollection.dup(n)
	}

	createNewUnitFromSymbol { |symbol|
		^ParU( n, symbol )
	}

	checkTarget { |target|
		var tsize = target.size;
		^(if( tsize == 0 ) {
			this.defaultTarget;
		} {
			if(target.maxDepth == 1){
				target.dup(n)
			} {
				assert(tsize != n, "ParUChain target must be of size % in ordet to match ParUChain number of parallel synths".format(n));
				target
			}
		});
	}

	// target :: [ [node](m - parallel servers) ] (n - paru multiplier)
	prepare { |target, startPos = 0, action|
		var cpu, chainAction, maction;
		//"entered ParUChain prepare %".format(this).postln;
		nowPreparingChain = this;

		// lastTarget = target;

		target = this.checkTarget(target);

		if( serverName.notNil ) {
			target = target.collect{ |target|
				target.select({ |trg|
					if( trg.isKindOf( LoadBalancer ).not ) {
						serverName.asCollection.includes( trg.asTarget.server.name );
					} {
						true;
					};
				})
			};
		};

		if( global ) {
			target = target.collect{ |target|
				target.collect({ |trg|
					if( trg.isKindOf( LoadBalancer ) ) {
						trg.servers;
					} {
						trg;
					};
				}).flat;
			}
		};

		//cpu = this.apxCPU;
		target = [target, ugroup.asParArg(n).array, parentUGroup.asParArg(n).array]
		.flopWith{ |target, ugroup, parentUGroup|
			target
			.select({ |tg|
				this.shouldPlayOn( tg ) != false;
			})
			.collect({ |tg|
				tg = UGroup.start( ugroup, tg, this, parentUGroup );
				tg = tg.asTarget;
				tg.server.loadBalancerAddLoad(this.apxCPU(tg));
				tg;
			});
		};
		preparedServers = target;

		maction = MultiActionFunc( {
			//"done ParUChain prepare %".format(this).postln;
			action.value
		} );
		//"ParUChain MultiActionFunc %".format(maction.hash).postln;
		//even if first action would fire immediatelly from the first unit this would still delay the actual action from being called
		chainAction = maction.getAction;

		//this will call prepare n times
		//target.do{ |target, i|
		units.do({ |u|
			//"ParUChain - Preparing unit %".format(u).postln;
			u.prepare(target, startPos, maction.getAction);
		});
		//};

		chainAction.value; // fire action at least once

		if( verbose ) { "% preparing for %".format( this, preparedServers ).postln; };

		nowPreparingChain = nil

		^target; // return array of actually prepared servers
	}

	// target :: [ [node](m - parallel servers) ] (n - paru multiplier)
	prepareAndStart{ |target, startPos = 0|
		var task, cond;
		if( target.isNil ) {
			target = this.checkTarget(target);
		};
		cond = Condition(false);
		task = fork {
			target = this.prepare( target, startPos, { cond.test = true; cond.signal } );
			cond.wait;
	       	this.start(target, startPos);
			if( releaseSelf.not ) {
				(this.duration + Server.default.latency).wait;
				this.release;
			};
	       	prepareTasks.remove(task);
		};
	    prepareTasks = prepareTasks.add( task );
	}

	prStartBasic { |target, startPos = 0, latency, withRelease = false|
		var targets, bundles;
		//"ParUChain - prStartBasicpr".postln;
		startPos = startPos ? 0;
		targets = preparedServers ?? { this.checkTarget(target) };
		preparedServers = nil;
		if( verbose ) { "% starting on %".format( this, targets ).postln; };
		latency = latency ?? { Server.default.latency; };
		units.do( _.modPerform(\start, startPos, latency) );
		bundles = this.makeBundle( targets, startPos , withRelease );
		[targets, bundles].flopWith{| innerT, innerB|
			[innerT, innerB].flopWith{| target, bundle|
				if( bundle.size > 0 ) {
					target.asTarget.server.sendSyncedBundle( latency, nil, *bundle );
				};
			}
		};
		^targets
	}

	dispose {
		units.do( _.dispose );
		preparedServers.do({ |srv|
			srv.collect{ |t|
				t.asTarget.server.loadBalancerAddLoad( this.apxCPU.neg )
			};
		});
		preparedServers = [];
	}

	makeBundle { |targets, startPos = 0, withRelease = false|
		var bundles;
		//var xxx = "makeBundle targets: %".format(targets).postln;
		this.setDoneAction;
		bundles = targets.collect({ |innertargets, i|
			ParU.currentIndex = i;
			innertargets.collect({ |target|
				target.asTarget.server.makeBundle( false, {
					this.makeGroupAndSynth(target, startPos);
					if( withRelease ) {
						this.release
					}
				})
			})
		});
		//var xxx1 = "makeBundle bundles: %".format(bundles).postln;
		if( verbose ) {
			("Bundles for "++this).postln;
			bundles.postln;
		};
		^bundles;
	}

	storeArgs { ^[n]++this.getInitArgs }

	getInitArgs {
		var numPreArgs = -1;
		if( releaseSelf != true ) {
			numPreArgs = 3
		} {
			if( duration != inf ) {
				numPreArgs = 2
			} {
				if( track != 0 ) {
					numPreArgs = 1
				} {
					if( startTime != 0 ) {
						numPreArgs = 0
					}
				}
			}
		};

		^([ startTime, track, duration, releaseSelf ][..numPreArgs]) ++
			units.collect({ |item|
				item = item.storeArgsWithoutN;
				if( item.size == 1 ) {
					item[0]
				} {
					item
				};
			});
	}

	gui { |parent, bounds, score| ^ParUChainGUI( parent, bounds, this, score ) }

	printOn { arg stream;
		stream << this.class.name << "( " << n << ", " <<* units.collect(_.defName)  << " )"
	}

}