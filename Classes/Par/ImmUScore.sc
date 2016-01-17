/*
		(C)opyright 2013-2015 by Miguel Negrão

    This file is part of ImmLib.

		ImmLib is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

		ImmLib is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImmLib.  If not, see <http://www.gnu.org/licenses/>.
*/

ImmUScore : UScore {

	var <surface;
	var <surfaceKey;

	*new { |surfaceKey ... args|
		var surface = PSurfaceDef.get(surfaceKey);
		var m = surface.size;

		var buses = surface.ubuses;
		var ugroups = ParArg( m.collect({ |i| ("immGroup"++i).asSymbol }) );
		var panners;
		var allEvents;
		var initArgs = [];
		var events;
		var duration;

		if( args[0].isNumber ) {
			initArgs = initArgs.add(args[0]);
			args = args[1..];
			if( args[0].isNumber ) {
				initArgs = initArgs.add(args[0]);
				args = args[1..];
				if( args[0].isArray ) {
					initArgs = initArgs.add(args[0]);
					args = args[1..];
				}
			}
		};

		events = args;

		duration = events.collect(_.endTime).maxItem ? 60;

		events.do{ |e|
			if(e.isKindOf(ParUChain)) {
				if(e.ugroup.isNil){
					e.ugroup_( ugroups )
				}
			}
		};

		allEvents =  switch(ImmLib.mode)
		{\normal}{
			switch( surface.renderMethod )
			{\vbap} {
				panners = ParUChain(surface.size, [\vbap3D_Simple_Panner,
					[\angles, surface.pointsDegrees, \spread, 0.0, \u_i_ar_0_bus, buses ]
				])
				.private_(true)
				.ugroup_(ugroups)
				.addAction_('addToTail')
				.hideInGUI_(true)
				.duration_(duration);
				events++panners;
			}
			{\direct } {
				panners = ParUChain(surface.size, [\output,
					[\bus, ParArg( surface.renderOptions.spkIndxs ), \u_i_ar_0_bus, surface.ubuses ]
				])
				.private_(true)
				.ugroup_(ugroups)
				.addAction_('addToTail')
				.hideInGUI_(true)
				.duration_(duration);
				events++panners;
			}{Error("renderMethod unknown: must be \vbap or \direct").throw}
		}
		{\previewBinaural} {
			var cipicBuffers = { |subjectID=1|
				["W","X","Y","Z"].collect{ |letter,i| 2.collect{ |j|
					var path = "%/FOA/decoders/cipic/44100/512/%/HRIR_%.wav".format(Atk.userKernelDir,subjectID.asString.padLeft(4, "0"), letter);
					["decoder_buf_%_%".format(i,j).asSymbol, BufSndFile(path, useChannels:[j]) ]
			}}.flat };
			panners = ParUChain(surface.size, [\immlib_binaural,
				cipicBuffers.(27)++[\angles, surface.pointsDegrees, \u_i_ar_0_bus, buses ]
			])
			.private_(true)
			.ugroup_(ugroups)
			.addAction_('addToTail')
			.hideInGUI_(true)
			.duration_(duration);
			events++panners;
		}
		{\previewStereo}{ events }
		{  Error("ImmLib.mode unknown : %.\nHas to be either \normal or \previewStereo or \previewBinaural !".format(ImmLib.mode)).throw }

		^super.new(*(initArgs++allEvents)).initImmUScore(surface, surfaceKey)

	}

	*getStringWithAllDefs {
		^"(\n"++
		PSurfaceDef.all.toTupleArray
		.collect{ |t| "PSurfaceDef("++t.at1.cs++","++t.at2.cs++");\n\n" }
		.mreduce++
		UScore.current.allEvents.collect{ |x| x.units.collect{ |y| y.mod } }
		.flat.select(_.notNil).collect(_.defName)
		.as(Set).as(Array)
		.collect{ |x| UEvNetModDef.all[x] }
		.select(_.notNil).collect{ |x| x.cs++";\n\n" }
		.mreduce++
		UScore.current
		.allEvents.collect{ |x| x.units.collect{ |y| y.def.name } }
		.flat
		.select{ |name|
			[\vbap3D_Simple_Panner, \stereoOutput, \pannerout]
			.includes(name).not
		}
		.removeDups.collect{ |x| Udef.all.at(x) }
		.select{ |y| y.notNil }
		.collect{ |x| x.cs++";\n\n" }
		.mreduce
		++"\n)"
	}

	/*saveAs { |path, successAction, cancelAction|
		var g = { |path|
			^super.saveAs(path, successAction, cancelAction);
			File.checkDo( path++".defs", { |f|
				f.write( ImmUScore.getStringWithAllDefs );
			}, overwrite, ask);
		};
		if( path.isNil ) {
			Dialog.savePanel(g);
		} {
			g.(path);
		};
	}*/

	initImmUScore{ |asurface, asurfaceKey|
		surface = asurface;
		surfaceKey = asurfaceKey;
	}

	getInitArgs {
		var numPreArgs = -1;

		if( extraResources.size > 0 ) {
			numPreArgs = 2
		} {
			if( track != 0 ) {
				numPreArgs = 1
			} {
				if( startTime != 0 ) {
					numPreArgs = 0
				}
			}
		};

		^([surfaceKey] ++ [ startTime, track, extraResources ][..numPreArgs]) ++ events.select{ |ev|
			((ev.class == ParUChain ) and: { ev.private }).not
		};
	}

	add{ |events|
		var m = surface.size;
		var ugroups = ParArg( m.collect({ |i| ("immGroup"++i).asSymbol }) );
		events.do{ |e|
			if(e.ugroup.isNil){
				e.ugroup_( ugroups )
			}
		};
		super.add(events)
	}

	cleanScoreOfParArgs {
		this.events.do{ |ev|
			if(ev.isKindOf(UChain)){
				var action = { |u|
					if( u.mod.notNil and:{ u.mod.isKindOf(ImmMod) }){
						u.mod.keySignalDict.keys.as(Array).do{ |key|
							u.set(key, u.def.getArgSpec(key).default)
						};
					}
				};
				ev.units.do{ |u|
					action.(u);
					u.getAllUMaps.do(action)
				};
				if(ev.ugroup.asString.contains("imm")){
					ev.ugroup_(nil)
				}
			}
		}
	}

}
