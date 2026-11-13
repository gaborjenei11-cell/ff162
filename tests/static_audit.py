from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
activity=(root/'app/src/main/java/audio/gsa/fieldforce/MainActivity.java').read_text()
bridge=(root/'app/src/main/java/audio/gsa/fieldforce/MediaSupport.java').read_text()
engine=(root/'app/src/main/java/audio/gsa/fieldforce/AudioEngine.java').read_text()
workflow=(root/'.github/workflows/build-apk.yml').read_text()
for token in ['FIELD RECORDER','SAMPLER','SEQUENCER','FX RACK','REC','METRONOME','TEMPO','SAVE PROJECT','LOAD PROJECT','SKINS (9)','EXPORT SEQUENCE WAV','EXIT','padImport(id)','NORMALIZE','REVERSE','TRIM','PRINT EFFECT']:
    assert token in activity,token
for token in ['MediaExtractor','MediaCodec','AudioRecord','AudioSource.MIC']:
    assert token in bridge,token
for token in ['AudioTrack','SequencerClock','renderBars','fx.process']:
    assert token in engine,token
assert 'engine.setPadFx' in activity and 'engine.setPadMute' in (root/'app/src/main/java/audio/gsa/fieldforce/AnalogControls.java').read_text()
assert 'new Spinner(this)' not in activity
assert 'GLOBAL FX' in activity
assert 'v1.4  •' not in activity
assert 'assembleDebug' in workflow and 'upload-artifact' in workflow
assert len(re.findall(r'"(Dark Graphite|Blue Alloy|Cream Metal|Walnut Glass|Violet Titanium|Solar Gold|Gunmetal Amber|Bronze Cyan|Pearl Magenta)"',activity))==9
assert (root/'app/src/main/res/drawable/ic_fieldforce.png').is_file()
assert (root/'app/src/main/res/drawable/gsa_logo.png').is_file()
print('PASS: UI pages, menu, 9 shared MOVE skins, audio API bridges, icon/logo, GitHub APK workflow')
