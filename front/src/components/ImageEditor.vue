<template>
    <div :class="{'page-wrapper': true, 'hide-bubble-preview': hideBubblePreview}">
        <div class="leftCol">
            <v-navigation-drawer :mini-variant="drawerMini" v-model="drawer"
                                 permanent>
                <v-list-item class="px-2">
                    <v-btn icon @click.stop="onSave">
                        <v-icon color="green">save</v-icon>
                    </v-btn>
                </v-list-item>
                <v-list-item class="px-2">
                    <v-btn icon @click.stop="onOpenIO">
                        <v-icon color="">import_export</v-icon>
                    </v-btn>
                </v-list-item>
                <v-list-item class="px-2">
                    <v-btn icon @click.stop="$emit('nextPage')">
                        <v-icon color="">forward</v-icon>
                    </v-btn>
                </v-list-item>
                <v-list-item class="px-2">
                    <v-btn icon @click="$emit('prevPage')">
                        <v-icon color="" style="transform: rotate(180deg)">forward</v-icon>
                    </v-btn>
                </v-list-item>
                <v-list-item class="px-2">
                    <v-btn icon v-if="hideBubblePreview" @click="hideBubblePreview=false">
                        <v-icon color="">visibility_off</v-icon>
                    </v-btn>
                    <v-btn icon v-else @click="hideBubblePreview=true">
                        <v-icon color="">visibility</v-icon>
                    </v-btn>
                </v-list-item>
                <v-divider/>
                <v-list-item class="px-2">
                    <v-btn icon @click="onClose">
                        <v-icon>close</v-icon>
                    </v-btn>
                </v-list-item>

            </v-navigation-drawer>
        </div>
        <div class="rightCol">
            <div style="height: 90px" class="top_bar">

            </div>
            <div class="image_container">
                <img id="image_target" :src="imageSrc" @load="imageLoaded = true">
                <div v-for="bubble of commitedBubbles" :key="bubble.id" class="commited_bubble"
                     v-bind:style="commitedStyles(bubble)"
                     @dblclick="toEditMode(bubble)"
                >

                    {{ bubble.text }}

                </div>
                <div v-if="editor" class="editor editor_bubble" :style="editorStyle">
                    <div class="editor__toolbar">
                        <editor-toolbar v-model="currentBubbleStyle"/>
                    </div>
                    <div class="overflow-hidden" style="width: 100%; height: 100%">
                        <div class="d-table" style="width: 100%; height: 100%">
                            <div :style="displayStyle" class="editor__textarea"
                                 @keydown.shift.enter.prevent="commit"
                                 ref="bubbleInputArea" contenteditable
                                 @input="handleTextInput"
                            >
                            </div>
                        </div>
                    </div>
                    <div class="editor_controls d-flex justify-space-between">
                        <v-btn color="red accent-3" fab small @click="deleteCurrent">
                            <v-icon>delete</v-icon>
                        </v-btn>
                        <v-btn color="light-green accent-3" fab small @click="commit">
                            <v-icon>done_outline</v-icon>
                        </v-btn>
                    </div>
                </div>
            </div>
        </div>
        <v-dialog v-model="io_dialog" scrollable max-width="600px">
            <v-card>
                <v-tabs>
                    <v-tab>Export</v-tab>
                    <v-tab-item>
                        <v-card-text class="mt-1 overflow-auto">
                            <div contenteditable style="white-space: pre-wrap; height: 500px" class="pa-1 mt-2">
                                {{ toExport }}
                            </div>
                        </v-card-text>
                    </v-tab-item>
                    <v-tab>Import</v-tab>
                    <v-tab-item>
                        <v-card-text class="mt-1">
                            <v-textarea v-model="toImport" full-width height="500px">
                            </v-textarea>
                            <div class="pa-1 mt-1 d-flex justify-end">
                                <v-btn color="primary" @click="runImport">Load</v-btn>
                            </div>
                        </v-card-text>
                    </v-tab-item>
                </v-tabs>
                <v-card-actions>
                    <v-btn color="secondary" @click="io_dialog = false">Close</v-btn>
                </v-card-actions>

            </v-card>
        </v-dialog>
    </div>
</template>

<script>

    import './ImageEditor.scss'
    import Vue from 'vue';
    import EditorToolbar from "./EditorToolbar";

    export default {
        name: "ImageEdit",
        props: {
            imageUrl: {
                type: String,
            },
            translation: {
                type: Object,
            }
        },
        components: {EditorToolbar},
        data() {
            return {
                imageSrc: this.imageUrl,
                imageLoaded: false,
                text: "",
                created: false,
                currentArea: null,
                currentBubbleId: null,
                bubbles: [],
                bubbleId: 0,
                $img: null,
                editor: false,
                defaultBubbleStyle: {
                    fontFamily: 'AnimeAce',
                    fontSize: 15,
                    bold: false,
                    italic: false,
                },
                currentBubbleStyle: {
                    fontSize: 15,
                    fontFamily: 'AnimeAce'
                },

                drawer: true,
                drawerMini: true,

                io_dialog: false,
                toExport: "",
                toImport: "",

                hideBubblePreview: false,
            }
        },
        created() {

        },
        watch: {
            imageLoaded(v) {
                this.$img = jQuery('#image_target');
                if (this.translation) {
                    this.onLoad(this.translation)
                }
                const self = this;
                this.$img.selectAreas({
                    ...this.selectorDefaultOptions(),
                    onChanging: (e, id, areas) => {
                        if (areas.length === 0) return;
                        self.currentArea = {
                            ...areas[0],
                        };
                    },
                    onChanged: (e, id, areas) => {
                        if (areas.length === 0) return;
                        self.currentArea = {
                            ...areas[0],
                        };
                        if (self.currentBubbleId === null) { // new bubble
                            self.currentBubbleId = self.nextId();
                        }
                        const text = self.text;
                        const mock = jQuery('.select-areas-background-area');
                        let mockText = mock.find('#bubble_text');
                        if (mockText.length === 0) {
                            mockText = $('<div class="bubble" id="bubble_text"></div>');
                            mock.append(mockText);
                            self.applyStylesToCurrentBubble(self.currentBubbleStyle);
                        }
                        mockText.text(text);
                        this.editor = true;
                    }
                });
            },
            text(newText) {
                jQuery('.bubble').text(newText)
            },
            editor(opened) {
                console.log(opened)
                if (opened) {
                    Vue.nextTick().then(() => {
                        this.$refs.bubbleInputArea.focus()
                    })
                }
            },
            displayStyle(newStyle) {
                this.applyStylesToCurrentBubble(newStyle);
            },
        },
        methods: {
            onClose() {
                if (this.$store.state.returnTo) {
                    this.$router.push(this.$store.state.returnTo);
                    return;
                }
                const seriesId = this.$route.params.seriesId;
                if (seriesId) {
                    this.$router.push({name: "SeriesEdit", params: {id: seriesId}})
                    return;
                }
            },
            handleTextInput(e) {
                this.text = e.target.innerText;
            },
            applyStylesToCurrentBubble(newStyle) {
                jQuery('#bubble_text').css({
                    'font-family': newStyle.fontFamily,
                    'font-size': newStyle.fontSize,
                })
            },
            commit() {
                this.$img.selectAreas('reset');

                const updatedObject = {
                    ...this.currentArea,
                    text: this.text,
                    id: this.currentBubbleId,
                    styles: this.displayStyle,
                };
                const currentBubble = this.currentBubble;
                if (!currentBubble) { // if new bubble
                    this.bubbles.push(updatedObject);
                } else {
                    const merged = {
                        ...currentBubble,
                        ...updatedObject,
                    };
                    const indexToInsert = this.bubbles.indexOf(currentBubble);
                    this.bubbles.splice(indexToInsert, 1, merged)
                }
                this.editor = false;
                this.currentBubbleId = null;
                this.currentArea = null;
                this.text = "";
            },
            deleteCurrent() {
                this.deleteIt(this.currentBubbleId);
            },
            deleteIt(id) {
                if (this.currentBubbleId === id) {
                    this.commit();
                }
                const index = this.bubbles.findIndex(b => b.id === id);
                this.bubbles.splice(index, N1);

            },
            nextId() {
                this.bubbleId++;
                return this.bubbleId
            },
            commitedStyles(bubble) {
                return {
                    ...this.buildBubblePositionStyles(bubble),
                    ...this.getStyle(bubble),
                }
            },

            buildBubblePositionStyles(bubble) {
                return {
                    top: bubble.y + 'px',
                    left: bubble.x + 'px',
                    height: bubble.height + 'px',
                    width: bubble.width + 'px',
                    zIndex: bubble.z,
                }
            },
            selectorDefaultOptions() {
                return {
                    minSize: [50, 50],
                    overlayOpacity: 0.0,
                    outlineOpacity: 0.9,
                    maxAreas: 1,
                    allowDelete: false,
                    allowNudge: false,
                }
            },
            toEditMode(bubble) {
                if (bubble.styles) {
                    this.currentBubbleStyle = this.cssToStylesMap({...bubble.styles}) ;
                } else {
                    this.currentBubbleStyle = {...this.defaultBubbleStyle}
                }
                this.openEditor(bubble);
                this.$img.selectAreas('add', {...bubble});

            },
            openEditor(bubble) {
                this.currentBubbleId = bubble.id;
                this.editor = true;
                this.text = bubble.text;
                Vue.nextTick().then(() => {
                    this.$refs.bubbleInputArea.innerText = this.text;
                })
            },
            getStyle(bubble) {
                if (bubble.styles) {
                    return bubble.styles;
                }
                return this.defaultStyle;
            },
            onLoad({annotations}) {
                const iWidth = this.$img.width();
                const iHeight = this.$img.height();
                let maxId = 0;
                this.bubbles = annotations.map((annotation) => {
                    const a = annotation.area_relative;
                    const x = Math.ceil(a.x * iWidth);
                    const width = Math.floor(a.width * iWidth);
                    const y = Math.ceil(a.y * iHeight);
                    const height = Math.floor(a.height * iHeight);
                    maxId = Math.max(maxId, annotation.id);
                    return {
                        id: annotation.id,
                        x, y, width, height,
                        z: 100 + annotation.id,
                        text: annotation.text,
                        styles: annotation.styles || this.defaultBubbleStyle,
                    }
                });
                this.bubbleId = maxId;
            },
            onOpenIO() {
                this.io_dialog = true;
                this.toExport = JSON.stringify(this.exportIt(), null, 2);
            },
            runImport() {
                if (!this.toImport) return;
                const model = JSON.parse(this.toImport);
                this.onLoad(model);
            },
            exportIt() {
                const iWidth = this.$img.width();
                const iHeight = this.$img.height();
                const annotations = this.bubbles.map((bubble) => {
                    const x = bubble.x / iWidth;
                    const width = bubble.width / iWidth;
                    const y = bubble.y / iHeight;
                    const height = bubble.height / iHeight;

                    return {
                        orig_image: {
                            width: iWidth,
                            height: iHeight,
                        },
                        id: bubble.id,
                        area: {x: bubble.x, y: bubble.y, width: bubble.width, height: bubble.height},
                        area_relative: {x, y, width, height},
                        text: bubble.text,
                        styles: {...bubble.styles}
                    }
                });
                const res = {annotations};
                return res;
            },
            onSave() {
                this.$emit('save', this.exportIt())
            },
            cssToStylesMap(css) {
                let fontSize = this.defaultBubbleStyle.fontSize;
                const fontSizeStr = css.fontSize;
                if (fontSizeStr) {
                    const m = fontSizeStr.match(/(\d+)/);
                    if (m) {
                        fontSize = parseInt(m[1], 10);
                    }
                }
                return {
                    ...css,
                    fontSize: fontSize,
                }
            }
        },
        computed: {
            commitedBubbles() {
                return this.bubbles.filter(b => b.id !== this.currentBubbleId)
            },
            currentBubble() {
                if (this.currentBubbleId === null) return null;
                return this.bubbles.find(b => b.id === this.currentBubbleId);
            },
            currentZone() {
                return this.currentArea || null;
            },
            hasEditor() {
                return this.editor;
            },
            editorStyle() {
                const baseBubble = this.currentZone;
                if (!baseBubble) return {};

                let w = Math.max(baseBubble.width, 100);
                let h = Math.max(baseBubble.height, 50);
                let z = baseBubble.z;
                let left = baseBubble.x - w - 20;
                let top = baseBubble.y;

                if (left < 0) {
                    left = baseBubble.x + baseBubble.width + 20;
                }

                return {
                    top: top + 'px',
                    left: left + 'px',
                    width: w + 'px',
                    height: h + 'px',
                    zIndex: 10000,
                }
            },
            defaultStyle() {
                let st = this.defaultBubbleStyle;

                const res = {
                    fontSize: st.fontSize + 'px'
                };
                if (st.italic) {
                    res.fontStyle = 'italic';
                }
                if (st.bold) {
                    res.fontWeight = 'bold';
                }
                res.fontFamily = st.fontFamily || 'auto';
                return res;
            },
            displayStyle() {
                let st = this.currentBubbleStyle;

                const res = {
                    fontSize: st.fontSize + 'px'
                };
                if (st.italic) {
                    res.fontStyle = 'italic';
                }
                if (st.bold) {
                    res.fontWeight = 'bold';
                }
                res.fontFamily = st.fontFamily || 'auto';
                return res;
            },

        }
    }
</script>

<style scoped lang="scss">
    .page-wrapper {
        display: flex;
        height: 100vh;
    }

    .leftCol {

    }

    .rightCol {
        overflow: auto;
        height: 100%;
    }

    .image_container {
        position: relative;
        max-width: 1000px;
    }

    #image_target {
        max-width: 1000px;
    }

    .editor {
        position: absolute;
        border: dashed black;

        textarea {
            text-align: center;
            height: 100%;
            outline: none;
            width: 100%;
            /*resize: none;*/
        }

    }

    .editor__toolbar {
        height: 30px;
        position: absolute;
        top: -90px;
        left: 0;
    }

    .editor__textarea {
        text-align: center;
        height: 100%;
        outline: none;
        display: table-cell;
        width: 100%;
        vertical-align: middle;
    }

    .editor_controls {
        position: absolute;
        bottom: -50px;
        right: 0;
        left: 0;
    }

    .top_bar {
        background: white;
        box-shadow: inset 0 -4px 7px 0px rgba(0, 0, 0, 0.45);
        height: 80px;
        z-index: 1000;

    }


</style>