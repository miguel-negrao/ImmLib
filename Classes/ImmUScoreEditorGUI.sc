ImmUScoreEditor : UScoreEditor {

	addEvent{
        this.changeScore({
			score.addEventToEmptyTrack( ImmUChain(score.surfaceKey,  [\immWhiteNoise, [], ImmMod(\wave2DSinForPlane) ]).startTime_(score.pos) )
        });
        score.changed(\numEventsChanged);
    }

}

+ ImmUScore {

	gui { |bounds|
		^UScoreEditorGUI( ImmUScoreEditor( this ), bounds );
	}
}	