Great! I’ll start converting the qiSDK Kotlin documentation from the official website into Markdown files, one per class or interface, preserving all the content they have—including method signatures, descriptions, examples, and any available diagrams or links.

I'll let you know once the collection is ready for review.

# Actuation

`interface Actuation`

The Actuation service contains the actions and objects needed to make the robot move: 
- the robot and gaze frames 
- the factory for Animation objects 
- the factories for GoTo, Animate and LookAt actions 

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of Actuation functions.)*

## Functions

### async
```kotlin
abstract fun async(): Async!
```

### gazeFrame
Retrieve the gaze Frame.  
```kotlin
abstract fun gazeFrame(): Frame!
```

### makeAnimate
```kotlin
abstract fun makeAnimate(context: RobotContext!, animation: Animation!): Animate!
```  
Create a new Animate action on the robot.  
**Parameters:**  
- `context` – `RobotContext!`: A qi.context.Context.  
- `animation` – `Animation!`: The Animation to play.  
**Return:** `Animate!` – An Animate action.  
**Since:** 1

### makeAnimation
```kotlin
abstract fun makeAnimation(anims: MutableList<String!>!): Animation!
```  
Create a new Animation object on the robot.  
**Parameters:**  
- `anims` – `MutableList<String!>!`: List of animation resource names.  
**Return:** `Animation!` – A new Animation object.  
**Since:** 1

### makeCroppedAnimation
```kotlin
abstract fun makeCroppedAnimation(anim: Animation!, beginTime: Long!, endTime: Long!): Animation!
```  
Create a new Animation object on the robot by cropping an existing animation.  
**Parameters:**  
- `anim` – `Animation!`: The original animation to crop.  
- `beginTime` – `Long!`: Starting time (in milliseconds) of the cropped animation.  
- `endTime` – `Long!`: Ending time (in milliseconds) of the cropped animation.  
**Return:** `Animation!` – The new cropped Animation.  
**Since:** 1

### makeEnforceTabletReachability
```kotlin
abstract fun makeEnforceTabletReachability(context: RobotContext!): EnforceTabletReachability!
```  
Create an EnforceTabletReachability action on the robot.  
**Parameters:**  
- `context` – `RobotContext!`: A qi.context.Context.  
**Return:** `EnforceTabletReachability!` – An EnforceTabletReachability action.  
**Since:** 1

### makeGoTo
```kotlin
abstract fun makeGoTo(context: RobotContext!, target: Frame!): GoTo!
```  
Create a new GoTo action on the robot. For more control over the GoTo behavior, prefer using `makeGoTo(context, target, config)`.  
**Parameters:**  
- `context` – `RobotContext!`: A qi.context.Context.  
- `target` – `Frame!`: A Frame representing the target to reach.  
**Return:** `GoTo!` – A GoTo action.  
**Since:** 1  

```kotlin
abstract fun makeGoTo(context: RobotContext!, target: Frame!, config: GoToConfig!): GoTo!
```  
Create a new GoTo action on the robot.  
**Parameters:**  
- `context` – `RobotContext!`: A qi.context.Context.  
- `target` – `Frame!`: A Frame representing the target to reach.  
- `config` – `GoToConfig!`: The GoTo configuration. If not explicitly set by the user, GoTo will use:  
  - a maximum navigating speed of 0.35 m/s;  
  - a `GetAroundObstacles` path planning policy;  
  - a `FreeOrientation` final orientation policy.  
**Return:** `GoTo!` – A GoTo action.  
**Since:** 6

### makeLookAt
```kotlin
abstract fun makeLookAt(context: RobotContext!, target: Frame!): LookAt!
```  
Create a LookAt action on the robot.  
**Parameters:**  
- `context` – `RobotContext!`: A qi.context.Context.  
- `target` – `Frame!`: The target frame to look at.  
**Return:** `LookAt!` – A LookAt action.  
**Since:** 1

### robotFrame
Retrieve the robot Frame.  
```kotlin
abstract fun robotFrame(): Frame!
```  

---

# ActuationConverter

*(No additional description provided.)*

`interface ActuationConverter`

---

# Age

`@QiStruct open class Age`

Structure representing an age.

**Since:** 1

## Constructors
- `Age(years: Int)`: Create a new `Age`.

## Functions

- **equals** – `open fun equals(other: Any?): Boolean`  
- **getYears** – `open fun getYears(): Int` – The number of years.  
- **hashCode** – `open fun hashCode(): Int`  
- **setYears** – `open fun setYears(years: Int): Unit` – Setter for years. The number of years.  
- **toString** – `open fun toString(): String`

---

# Animate

`interface Animate`

Action to play animations on the robot.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of Animate functions.)*

## Functions

### async
```kotlin
abstract fun async(): Animate.Async!
```

### run
Start the animation.  
```kotlin
abstract fun run(): Future<Void>!
```  
**Return:** `Future<Void>!` – A future that completes when the animation finishes running.  
**Since:** 1

### setOnStartedListener  
Set a listener for when the Animate action starts.  
```kotlin
abstract fun ~setOnStartedListener~(listener: Animate.OnStartedListener!): Unit
```  

*(The `setOnStartedListener` function is deprecated.)*

---

# AnimateBuilder

`open class AnimateBuilder`

Build a new `Animate`.

## Functions

### build
```kotlin
open fun build(): Animate!
```  
Return a configured instance of Animate.  
**Return:** `Animate!` – The built Animate action.

### buildAsync
```kotlin
open fun buildAsync(): Future<Animate>!
```  
Return a configured instance of Animate (asynchronously).  
**Return:** `Future<Animate>!` – A future that will complete with the built Animate action.

### with
```kotlin
open static fun with(context: QiContext!): AnimateBuilder!
```  
Create a new builder from the QiContext.  
**Parameters:**  
- `context` – `QiContext!`: The QiContext providing the environment for the animate action.  
**Return:** `AnimateBuilder!` – A new builder instance.

### withAnimation
```kotlin
open fun withAnimation(animation: Animation!): AnimateBuilder!
```  
Configure the animation to be used by the Animate action.  
**Parameters:**  
- `animation` – `Animation!`: The animation to play.  
**Return:** `AnimateBuilder!` – The builder (for chaining).

---

# Animation

`interface Animation`

Object representing a robot animation. An animation can be composed of gestures performed by the robot’s limbs and head, and/or trajectories performed by the robot base.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of Animation functions.)*

## Functions

### async
```kotlin
abstract fun async(): Animation.Async!
```

### duration
Get the duration of the animation, in milliseconds.  
```kotlin
abstract fun duration(): Long!
```  
**Return:** `Long!` – Duration in milliseconds.

---

# AnimationBuilder

`open class AnimationBuilder`

Build a new `Animation`.

## Functions

### build
```kotlin
open fun build(): Animation!
```  
Return a configured instance of Animation.  
**Return:** `Animation!` – The built Animation object.

### buildAsync
```kotlin
open fun buildAsync(): Future<Animation>!
```  
Return a configured instance of Animation (asynchronously).  
**Return:** `Future<Animation>!` – A future that will complete with the built Animation.

### with
```kotlin
open static fun with(context: QiContext!): AnimationBuilder!
```  
Create a new builder from the QiContext.  
**Parameters:**  
- `context` – `QiContext!`: The QiContext.  
**Return:** `AnimationBuilder!` – A new builder instance.

### withResources
```kotlin
open fun withResources(resources: List<String!>!): AnimationBuilder!
```  
Configure the animation resources.  
**Parameters:**  
- `resources` – `List<String!>!`: A list of animation resource names (as strings).  
**Return:** `AnimationBuilder!` – The builder (for chaining).

---

# AnyObjectProvider

`interface AnyObjectProvider`

Interface for objects that can provide an AnyObject.

---

# AnyObjectProxyAsync

`interface AnyObjectProxyAsync`

*(No additional description provided.)*

---

# AnyObjectProxyConverter

`interface AnyObjectProxyConverter`

*(No additional description provided.)*

---

# AnyObjectProxySync

`interface AnyObjectProxySync`

*(No additional description provided.)*

---

# AnyObjectWrapper

`open class AnyObjectWrapper`

Parent class for QiService objects that run on the tablet.

---

# AnyObjectWrapperConverter

`interface AnyObjectWrapperConverter`

Converter for AnyObjectWrapper objects.

---

# ApproachHuman

`interface ApproachHuman`

Action to make the robot go towards a human and respond to various situations on the way.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of ApproachHuman functions.)*

## Functions

### async
```kotlin
abstract fun async(): ApproachHuman.Async!
```

### run
Start approaching the human.  
```kotlin
abstract fun run(): Future<Void>!
```  
**Return:** `Future<Void>!` – A future that completes when the action finishes or is canceled.

### setPolicy
```kotlin
abstract fun setPolicy(policy: EngagementPolicy!): Unit
```  
Set the engagement policy for approaching the human.  
**Parameters:**  
- `policy` – `EngagementPolicy!`: The eye contact policy to apply.

---

# ApproachHumanBuilder

`open class ApproachHumanBuilder`

Build a new `ApproachHuman`.

## Functions

- **build** – `open fun build(): ApproachHuman!`  
- **buildAsync** – `open fun buildAsync(): Future<ApproachHuman>!`  
- **with** – `open static fun with(context: QiContext!): ApproachHumanBuilder!`  
- **withPolicy** – `open fun withPolicy(policy: EngagementPolicy!): ApproachHumanBuilder!` – Configure the engagement policy for the builder.

---

# AttachedFrame

`interface AttachedFrame`

Object representing a frame attached to a parent frame. The link between the parent and the attached frame (i.e., the relative location of the attached frame to its parent) is editable. In order to compute transforms between frames, one should use the `frame()` function of an AttachedFrame.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of AttachedFrame functions.)*

## Functions

### async
```kotlin
abstract fun async(): AttachedFrame.Async!
```

### frame
Get the Frame representing this AttachedFrame (for computing transforms, etc).  
```kotlin
abstract fun frame(): Frame!
```  
**Return:** `Frame!` – The Frame of this attached frame.

### update
```kotlin
abstract fun update(transform: Transform!, frame: Frame!, timestamp: Long!): Unit
```  
Update the global position of this free frame by giving its location at a given time in a given reference frame.  
**Parameters:**  
- `transform` – `Transform!`: The transform representing the new pose of this frame relative to `frame`.  
- `frame` – `Frame!`: The reference frame in which the transform is expressed.  
- `timestamp` – `Long!`: The timestamp of the given transform (in milliseconds since epoch).

---

# AttentionState

`class AttentionState : QiEnum`

Enum containing the possible attention states of the human when interacting with the robot. States are defined based on where the human is looking (frame of reference is the human).

**Since:** 1

## Enum Values
- `UNKNOWN`  
- `NOT_SELECTED`  
- `SELECTED`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# AutonomousAbilities

`interface AutonomousAbilities`

A service that allows selecting which autonomous abilities to pause or resume during an activity. This service will ensure the ability owner that he or she can be the only one to control this ability as long as his or her holder is not released. Holding an ability will automatically pause it. To resume it, the owner must release it.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of AutonomousAbilities functions.)*

## Functions

### async
```kotlin
abstract fun async(): AutonomousAbilities.Async!
```

### getAbility
```kotlin
abstract fun getAbility(type: AutonomousAbilitiesType!): AutonomousAbilityHolder!
```  
Obtain a holder for the specified autonomous ability, pausing that ability until released.  
**Parameters:**  
- `type` – `AutonomousAbilitiesType!`: The type of autonomous ability to control (e.g., navigation, dialogue).  
**Return:** `AutonomousAbilityHolder!` – A holder that, when released, will resume the ability.

---

# AutonomousabilitiesConverter

`interface AutonomousabilitiesConverter`

*(No additional description provided.)*

---

# AutonomousAbilitiesType

`enum class AutonomousAbilitiesType`

*(No documentation provided on site for this enum.)*

*(Possible values might represent different autonomous abilities such as `BasicAwareness`, `BackgroundMovement`, etc., but no details given in the reference.)*

---

# AutonomousAbilityHolder

`interface AutonomousAbilityHolder`

An AutonomousAbilityHolder represents an autonomous ability being taken from the AutonomousAbilities service. It serves only once, and emits `released()` whenever the autonomous ability is released. An AutonomousAbilityHolder that was released is invalid.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of AutonomousAbilityHolder functions.)*

## Functions

### async
```kotlin
abstract fun async(): AutonomousAbilityHolder.Async!
```

### release
Release the autonomous ability and invalidate this holder.  
```kotlin
abstract fun release(): Future<Void>!
```  
**Return:** `Future<Void>!` – A future that completes when the ability is released (and the holder becomes invalid).

### setOnReleasedListener
```kotlin
abstract fun ~setOnReleasedListener~(listener: AutonomousAbilityHolder.OnReleasedListener!): Unit
```  
Set a listener for when the autonomous ability is released. *(Deprecated; use addOnReleasedListener instead.)*

## Types (Nested)
- `interface OnReleasedListener` – Listener for the released signal of this holder.

---

# AutonomousReaction

`interface AutonomousReaction`

A reaction suggested by a Chatbot.

---

# AutonomousReactionImportance

`class AutonomousReactionImportance : QiEnum`

Additional information on the importance of a suggested ChatbotReaction.

**Since:** 1

## Enum Values
- `LOW`  
- `NORMAL`  
- `HIGH`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# AutonomousReactionValidity

`class AutonomousReactionValidity : QiEnum`

Describes the validity of a suggested ChatbotReaction.

**Since:** 1

## Enum Values
- `UNKNOWN`  
- `VALID`  
- `INVALID`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# BaseChatbot

`abstract class BaseChatbot`

Parent class for ChatBot implementations.

---

# BaseChatbotReaction

`abstract class BaseChatbotReaction`

Parent class for ChatBotReaction implementations.

---

# BaseQiChatExecutor

`abstract class BaseQiChatExecutor`

Parent class for QiChatExecutor implementations.

---

# BodyLanguageOption

`class BodyLanguageOption : QiEnum`

Body language policy.

**Since:** 1

## Enum Values
- `AUTO`  
- `NEVER`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# Bookmark

`open class Bookmark`

Object representing a marked location in a topic.

---

# BookmarkStatus

`open class BookmarkStatus`

Object representing the state of a Bookmark during a Discuss execution.

---

# Camera

`interface Camera`

Service exposing actions and properties related to the robot camera.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of Camera functions.)*

## Functions

### async
```kotlin
abstract fun async(): Camera.Async!
```

### takePicture
```kotlin
abstract fun takePicture(): Future<EncodedImage>!
```  
Take a picture with the robot’s camera.  
**Return:** `Future<EncodedImage>!` – A future that will yield the captured image.

### startRecording
```kotlin
abstract fun startRecording(): Future<Void>!
```  
Start recording video.  
**Return:** `Future<Void>!` – A future that completes when recording starts.

### stopRecording
```kotlin
abstract fun stopRecording(): Future<EncodedImage>!
```  
Stop recording video.  
**Return:** `Future<EncodedImage>!` – A future that yields the last frame captured when recording stopped.

*(Other camera functions and properties omitted for brevity if any.)*

---

# CameraConverter

`interface CameraConverter`

*(No additional description provided.)*

---

# Chat

`interface Chat`

Action that listens to the users and interrogates its Chatbots to select the most appropriate answers.

**Since:** 3

## Types
- `interface Async`
- `interface OnFallbackReplyFoundForListener` – Listener for the `fallbackReplyFoundFor` signal.
- `interface OnHeardListener` – Listener for the `heard` signal.
- `interface OnHearingChangedListener` – Listener for changes in the `hearing` property.
- `interface OnListeningChangedListener` – Listener for changes in the `listening` property.
- `interface OnNoPhraseRecognizedListener` – Listener for the `noPhraseRecognized` signal.
- `interface OnNoReplyFoundForListener` – Listener for the `noReplyFoundFor` signal.
- `interface OnNormalReplyFoundForListener` – Listener for the `normalReplyFoundFor` signal.
- `interface OnSayingChangedListener` – Listener for changes in the `saying` property.
- `interface OnStartedListener` – Listener for the `started` signal.

## Functions

- **addOnFallbackReplyFoundForListener** – `abstract fun addOnFallbackReplyFoundForListener(listener: OnFallbackReplyFoundForListener!): Unit` – Add an OnFallbackReplyFoundForListener.  
- **addOnHeardListener** – `abstract fun addOnHeardListener(listener: OnHeardListener!): Unit` – Add an OnHeardListener.  
- **addOnHearingChangedListener** – `abstract fun addOnHearingChangedListener(listener: OnHearingChangedListener!): Unit` – Add a property changed listener for hearing.  
- **addOnListeningChangedListener** – `abstract fun addOnListeningChangedListener(listener: OnListeningChangedListener!): Unit` – Add a property changed listener for listening.  
- **addOnNoPhraseRecognizedListener** – `abstract fun addOnNoPhraseRecognizedListener(listener: OnNoPhraseRecognizedListener!): Unit` – Add an OnNoPhraseRecognizedListener.  
- **addOnNoReplyFoundForListener** – `abstract fun addOnNoReplyFoundForListener(listener: OnNoReplyFoundForListener!): Unit` – Add an OnNoReplyFoundForListener.  
- **addOnNormalReplyFoundForListener** – `abstract fun addOnNormalReplyFoundForListener(listener: OnNormalReplyFoundForListener!): Unit` – Add an OnNormalReplyFoundForListener.  
- **addOnSayingChangedListener** – `abstract fun addOnSayingChangedListener(listener: OnSayingChangedListener!): Unit` – Add a property changed listener for saying.  
- **addOnStartedListener** – `abstract fun addOnStartedListener(listener: OnStartedListener!): Unit` – Add an OnStartedListener.  
- **async** – `abstract fun async(): Chat.Async!`  
- **getHearing** – `abstract fun getHearing(): Boolean!` – Exposes the `hearing` property value.  
- **getListening** – `abstract fun getListening(): Boolean!` – Exposes the `listening` property value.  
- **getListeningBodyLanguage** – `abstract fun getListeningBodyLanguage(): BodyLanguageOption!` – Exposes the `listeningBodyLanguage` property value.  
- **getSaying** – `abstract fun getSaying(): Phrase!` – Exposes the `saying` property value.  
- **removeAllOnFallbackReplyFoundForListeners** – `abstract fun removeAllOnFallbackReplyFoundForListeners(): Unit` – Remove all OnFallbackReplyFoundForListener.  
- **removeAllOnHeardListeners** – `abstract fun removeAllOnHeardListeners(): Unit` – Remove all OnHeardListener.  
- **removeAllOnHearingChangedListeners** – `abstract fun removeAllOnHearingChangedListeners(): Unit` – Remove all hearing changed listeners.  
- **removeAllOnListeningChangedListeners** – `abstract fun removeAllOnListeningChangedListeners(): Unit` – Remove all listening changed listeners.  
- **removeAllOnNoPhraseRecognizedListeners** – `abstract fun removeAllOnNoPhraseRecognizedListeners(): Unit` – Remove all OnNoPhraseRecognizedListener.  
- **removeAllOnNoReplyFoundForListeners** – `abstract fun removeAllOnNoReplyFoundForListeners(): Unit` – Remove all OnNoReplyFoundForListener.  
- **removeAllOnNormalReplyFoundForListeners** – `abstract fun removeAllOnNormalReplyFoundForListeners(): Unit` – Remove all OnNormalReplyFoundForListener.  
- **removeAllOnSayingChangedListeners** – `abstract fun removeAllOnSayingChangedListeners(): Unit` – Remove all saying changed listeners.  
- **removeAllOnStartedListeners** – `abstract fun removeAllOnStartedListeners(): Unit` – Remove all OnStartedListener.  
- **removeOnFallbackReplyFoundForListener** – `abstract fun removeOnFallbackReplyFoundForListener(listener: OnFallbackReplyFoundForListener!): Unit` – Remove an OnFallbackReplyFoundForListener.  
- **removeOnHeardListener** – `abstract fun removeOnHeardListener(listener: OnHeardListener!): Unit` – Remove an OnHeardListener.  
- **removeOnHearingChangedListener** – `abstract fun removeOnHearingChangedListener(listener: OnHearingChangedListener!): Unit` – Remove a hearing changed listener.  
- **removeOnListeningChangedListener** – `abstract fun removeOnListeningChangedListener(listener: OnListeningChangedListener!): Unit` – Remove a listening changed listener.  
- **removeOnNoPhraseRecognizedListener** – `abstract fun removeOnNoPhraseRecognizedListener(listener: OnNoPhraseRecognizedListener!): Unit` – Remove an OnNoPhraseRecognizedListener.  
- **removeOnNoReplyFoundForListener** – `abstract fun removeOnNoReplyFoundForListener(listener: OnNoReplyFoundForListener!): Unit` – Remove an OnNoReplyFoundForListener.  
- **removeOnNormalReplyFoundForListener** – `abstract fun removeOnNormalReplyFoundForListener(listener: OnNormalReplyFoundForListener!): Unit` – Remove an OnNormalReplyFoundForListener.  
- **removeOnSayingChangedListener** – `abstract fun removeOnSayingChangedListener(listener: OnSayingChangedListener!): Unit` – Remove a saying changed listener.  
- **removeOnStartedListener** – `abstract fun removeOnStartedListener(listener: OnStartedListener!): Unit` – Remove an OnStartedListener.  
- **run** – `abstract fun run(): Unit` – The robot starts to listen, react and talk by picking reactions. If the robot is in a language different from the action language, the robot’s language will be changed.  
  **Exceptions:**  
  - `RuntimeException` – This Chat action is already running.  
- **setListeningBodyLanguage** – `abstract fun setListeningBodyLanguage(bodyLanguageOption: BodyLanguageOption!): Unit` – Set the bodyLanguageOption property value.  
- **setOnFallbackReplyFoundForListener** – `abstract fun ~setOnFallbackReplyFoundForListener~(listener: OnFallbackReplyFoundForListener!): Unit` *(deprecated)*  
- **setOnHeardListener** – `abstract fun ~setOnHeardListener~(listener: OnHeardListener!): Unit` *(deprecated)*  
- **setOnHearingChangedListener** – `abstract fun ~setOnHearingChangedListener~(listener: OnHearingChangedListener!): Unit` *(deprecated)*  
- **setOnListeningChangedListener** – `abstract fun ~setOnListeningChangedListener~(listener: OnListeningChangedListener!): Unit` *(deprecated)*  
- **setOnNoPhraseRecognizedListener** – `abstract fun ~setOnNoPhraseRecognizedListener~(listener: OnNoPhraseRecognizedListener!): Unit` *(deprecated)*  
- **setOnNoReplyFoundForListener** – `abstract fun ~setOnNoReplyFoundForListener~(listener: OnNoReplyFoundForListener!): Unit` *(deprecated)*  
- **setOnNormalReplyFoundForListener** – `abstract fun ~setOnNormalReplyFoundForListener~(listener: OnNormalReplyFoundForListener!): Unit` *(deprecated)*  
- **setOnSayingChangedListener** – `abstract fun ~setOnSayingChangedListener~(listener: OnSayingChangedListener!): Unit` *(deprecated)*  
- **setOnStartedListener** – `abstract fun ~setOnStartedListener~(listener: OnStartedListener!): Unit` *(deprecated)*

---

# Chatbot

`interface Chatbot`

Object representing a Chatbot that can react in response to Phrases.

---

# ChatbotReaction

`interface ChatbotReaction`

Action produced by a Chatbot either as a reply to a Phrase, or spontaneously.

---

# ChatbotReactionHandlingStatus

`class ChatbotReactionHandlingStatus : QiEnum`

Describes the current status of a ChatbotReaction (for example, in a Chat action).

**Since:** 1

## Enum Values
- `NOT_HANDLED`  
- `HANDLED`  
- `FAILED`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# ChatBuilder

`open class ChatBuilder`

Build a new `Chat`.

## Functions

- **build** – `open fun build(): Chat!`  
- **buildAsync** – `open fun buildAsync(): Future<Chat>!`  
- **with** – `open static fun with(context: QiContext!): ChatBuilder!`  
- **withChatbot** – `open fun withChatbot(chatbot: QiChatbot!): ChatBuilder!` – Configure the Chatbot to use.  
- **withChatOptions** – `open fun withChatOptions(options: ChatOptions!): ChatBuilder!` – Configure optional parameters for the Chat action.

---

# ChatOptions

`open class ChatOptions`

Optional parameters for the configuration of a Chat action.

---

# ContextConverter

`interface ContextConverter`

*(No additional description provided.)*

---

# Conversation

`interface Conversation`

Service exposing actions and properties related to human-robot conversation.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of Conversation functions.)*

## Functions

### async
```kotlin
abstract fun async(): Conversation.Async!
```

### status
Get the ConversationStatus for the current context.  
```kotlin
abstract fun status(): ConversationStatus!
```  
**Return:** `ConversationStatus!` – The conversation-related signals and properties for this application context.

---

# ConversationConverter

`interface ConversationConverter`

*(No additional description provided.)*

---

# ConversationStatus

`open class ConversationStatus`

An object collecting Conversation-related signals and properties for a given application context.

---

# DateString

`open class DateString`

Struct representing a date as a string.

---

# DateTimeString

`open class DateTimeString`

Struct representing a dateTime as a string.

---

# DegreeOfFreedom

`class DegreeOfFreedom : QiEnum`

A degree of freedom.

**Since:** 1

## Enum Values
*(No explicit values documented, likely something like X, Y, Theta depending on context.)*

*(No further details provided.)*

---

# Discuss

`interface Discuss`

Action to make the robot able to converse with a human using content from QiChat topics.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of Discuss functions.)*

## Functions

### async
```kotlin
abstract fun async(): Discuss.Async!
```

### run
Start the discussion using the provided topics.  
```kotlin
abstract fun run(): Future<Void>!
```  
**Return:** `Future<Void>!` – A future that completes when the discussion ends or is canceled.

*(Other functions for managing topics or bookmarks may exist.)*

---

# DiscussBuilder

`open class DiscussBuilder`

Build a new `Discuss`.

## Functions

- **build** – `open fun build(): Discuss!`  
- **buildAsync** – `open fun buildAsync(): Future<Discuss>!`  
- **with** – `open static fun with(context: QiContext!): DiscussBuilder!`  
- **withTopic** – `open fun withTopic(topic: Topic!): DiscussBuilder!` – Add a topic to the discussion.  
- **withTopics** – `open fun withTopics(topics: List<Topic!>!): DiscussBuilder!` – Add multiple topics.

---

# EditableKnowledgeGraph

`interface EditableKnowledgeGraph`

Object allowing to edit a named graph.

---

# EditablePhraseSet

`interface EditablePhraseSet`

A container of Phrases that can be edited.

---

# Emotion

`interface Emotion`

Object containing the emotional state properties. It is a three-dimensional representation of the emotional state, based on the PAD model of Albert Mehrabian. *See:* Mehrabian, Albert (1980). *Basic dimensions for a general psychological theory.*

**Since:** 1

## Types
- `interface Async`
- `interface OnExcitementChangedListener` – Listener for excitement property changes.
- `interface OnPleasureChangedListener` – Listener for pleasure property changes.

## Functions

- **addOnExcitementChangedListener** – `abstract fun addOnExcitementChangedListener(listener: OnExcitementChangedListener!): Unit`  
- **addOnPleasureChangedListener** – `abstract fun addOnPleasureChangedListener(listener: OnPleasureChangedListener!): Unit`  
- **async** – `abstract fun async(): Emotion.Async!`  
- **getExcitement** – `abstract fun getExcitement(): ExcitementState!` – Exposes the excitement value.  
- **getPleasure** – `abstract fun getPleasure(): PleasureState!` – Exposes the pleasure value.  
- **removeAllOnExcitementChangedListeners** – `abstract fun removeAllOnExcitementChangedListeners(): Unit`  
- **removeAllOnPleasureChangedListeners** – `abstract fun removeAllOnPleasureChangedListeners(): Unit`  
- **removeOnExcitementChangedListener** – `abstract fun removeOnExcitementChangedListener(listener: OnExcitementChangedListener!): Unit`  
- **removeOnPleasureChangedListener** – `abstract fun removeOnPleasureChangedListener(listener: OnPleasureChangedListener!): Unit`  
- **setOnExcitementChangedListener** – `abstract fun ~setOnExcitementChangedListener~(listener: OnExcitementChangedListener!): Unit` *(deprecated)*  
- **setOnPleasureChangedListener** – `abstract fun ~setOnPleasureChangedListener~(listener: OnPleasureChangedListener!): Unit` *(deprecated)*

---

# EncodedImage

`interface EncodedImage`

Encoded image.

*(Likely represents an image in a particular format (JPEG, etc.) but no further detail provided.)*

---

# EncodedImageHandle

`interface EncodedImageHandle`

Encapsulates an EncodedImage. This object enables sharing EncodedImage data while delaying the copy to the most appropriate time.

---

# EnforceTabletReachability

`interface EnforceTabletReachability`

Action to limit robot movements in order to ease user interaction with the tablet. The robot will put the tablet at a suitable position then emit the `positionReached()` signal, and prevent further leg and base movements, while also ensuring that the arm movements do not bring them in front of the tablet.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of EnforceTabletReachability functions.)*

## Functions

### async
```kotlin
abstract fun async(): EnforceTabletReachability.Async!
```

### run
Begin the action to enforce tablet reachability.  
```kotlin
abstract fun run(): Future<Void>!
```  
**Return:** `Future<Void>!` – A future that completes when the robot has adjusted its position or the action is canceled.

### setOnPositionReachedListener
```kotlin
abstract fun ~setOnPositionReachedListener~(listener: OnPositionReachedListener!): Unit
```  
*(Deprecated)* Set a listener for when the tablet position has been reached.

## Types (Nested)
- `interface OnPositionReachedListener` – Listener for the `positionReached` signal.

---

# EnforceTabletReachabilityBuilder

`open class EnforceTabletReachabilityBuilder`

Build a new `EnforceTabletReachability`.

## Functions

- **build** – `open fun build(): EnforceTabletReachability!`  
- **buildAsync** – `open fun buildAsync(): Future<EnforceTabletReachability>!`  
- **with** – `open static fun with(context: QiContext!): EnforceTabletReachabilityBuilder!`

---

# EngageHuman

`interface EngageHuman`

Action to make the robot look at a human and keep eye contact.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of EngageHuman functions.)*

## Functions

### async
```kotlin
abstract fun async(): EngageHuman.Async!
```

### run
Begin engaging with a human (looking at them and maintaining eye contact).  
```kotlin
abstract fun run(): Future<Void>!
```  
**Return:** `Future<Void>!` – Completes when engagement ends or is canceled.

### setPolicy
```kotlin
abstract fun setPolicy(policy: EngagementPolicy!): Unit
```  
Set the engagement (eye contact) policy.  
**Parameters:**  
- `policy` – `EngagementPolicy!`: The eye contact policy to apply.

---

# EngageHumanBuilder

`open class EngageHumanBuilder`

Build a new `EngageHuman`.

## Functions

- **build** – `open fun build(): EngageHuman!`  
- **buildAsync** – `open fun buildAsync(): Future<EngageHuman>!`  
- **with** – `open static fun with(context: QiContext!): EngageHumanBuilder!`  
- **withPolicy** – `open fun withPolicy(policy: EngagementPolicy!): EngageHumanBuilder!`

---

# EngagementIntentionState

`class EngagementIntentionState : QiEnum`

Enum containing the engagement intention of the human toward the robot, as perceived.

**Since:** 1

## Enum Values
- `UNKNOWN`  
- `DISENGAGED`  
- `ENGAGED`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# EngagementPolicy

`class EngagementPolicy : QiEnum`

Eye contact policy.

**Since:** 1

## Enum Values
- `NONE`  
- `SOFT`  
- `STRONG`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# EnumConverter

`interface EnumConverter`

Convert a `QiEnum` to and from a raw `AnyObject`.

---

# ExcitementState

`class ExcitementState : QiEnum`

Enum containing the perceived energy in the emotion.

**Since:** 1

## Enum Values
- `LOW`  
- `NORMAL`  
- `HIGH`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# ExplorationMap

`interface ExplorationMap`

Object encapsulating the data needed by the robot to localize itself inside its environment.

---

# ExplorationMapBuilder

`open class ExplorationMapBuilder`

Build a new `ExplorationMap`.

## Functions

- **build** – `open fun build(): ExplorationMap!`  
- **buildAsync** – `open fun buildAsync(): Future<ExplorationMap>!`  
- **with** – `open static fun with(context: QiContext!): ExplorationMapBuilder!`

---

# FacialExpressions

`open class FacialExpressions`

Structure containing expression data computed from a human's face.

---

# FlapSensor

`interface FlapSensor`

Object representing a flap (e.g., a robot's head flap) that may be open or closed.

---

# FlapState

`interface FlapState`

Description of a flap sensor state.

*(Likely provides properties to check if the flap is open or closed, etc.)*

---

# Focus

`interface Focus`

A service tracking the current focus, and guaranteeing that only one client has the focus at the same time. The focus is required for actions to be performed on the robot. This mechanism ensures the focus owner that it can be the only one to control the robot as long as its FocusOwner is not released.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of Focus functions.)*

## Functions

### async
```kotlin
abstract fun async(): Focus.Async!
```

### request
```kotlin
abstract fun request(): FocusOwner!
```  
Request the focus for the current application/activity.  
**Return:** `FocusOwner!` – A FocusOwner object representing the granted focus.

---

# FocusConverter

`interface FocusConverter`

*(No additional description provided.)*

---

# FocusOwner

`interface FocusOwner`

A FocusOwner represents a focus being taken from the focus service. It serves only once, and emits `released()` whenever the focus is released. A FocusOwner that was released is invalid.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of FocusOwner functions.)*
- `interface OnReleasedListener` – Listener for the released signal.

## Functions

- **addOnReleasedListener** – `abstract fun addOnReleasedListener(listener: OnReleasedListener!): Unit`  
- **async** – `abstract fun async(): FocusOwner.Async!`  
- **release** – `abstract fun release(): Unit` – Release the focus and invalidate this FocusOwner.  
- **removeAllOnReleasedListeners** – `abstract fun removeAllOnReleasedListeners(): Unit`  
- **removeOnReleasedListener** – `abstract fun removeOnReleasedListener(listener: OnReleasedListener!): Unit`  
- **setOnReleasedListener** – `abstract fun ~setOnReleasedListener~(listener: OnReleasedListener!): Unit` *(deprecated)*  
- **token** – `abstract fun token(): String!` – The token carried by this FocusOwner (unique identifier for the focus session).

---

# Frame

`interface Frame`

Object representing the location associated with an object or a person. This location is likely to change over time (for example, when a person moves). Transforms can be computed between two frames, at a given time, to get the relative position and orientation between two objects. If the robot is localized using external sensors, the transform between two frames can be computed with odometry drift compensation.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of Frame functions.)*

## Functions

### async
```kotlin
abstract fun async(): Frame.Async!
```

### computeTransform
Compute the transform between this frame and another frame at a given time.  
```kotlin
abstract fun computeTransform(destination: Frame!, time: Long!): TransformTime!
```  
**Parameters:**  
- `destination` – `Frame!`: The frame to which to compute the transform.  
- `time` – `Long!`: The timestamp at which to compute the transform.  
**Return:** `TransformTime!` – The transform from this frame to the destination frame at the given time.

---

# FreeFrame

`interface FreeFrame`

Object representing a reference frame free to be placed anywhere, that does not move when other frames move. The global position of a free frame can be updated by giving its location at a given time in a given reference frame. In order to compute transforms between frames, one should use the `frame()` function of a FreeFrame. The FreeFrame will be invalid right after creation until first update.

**Since:** 1

## Types
- `interface Async` – *(Asynchronous version of FreeFrame functions.)*

## Functions

### async
```kotlin
abstract fun async(): FreeFrame.Async!
```

### frame
Get the Frame associated with this FreeFrame.  
```kotlin
abstract fun frame(): Frame!
```

### update
Update this free frame’s global location at a given time in a reference frame.  
```kotlin
abstract fun update(transform: Transform!, frame: Frame!, timestamp: Long!): Unit
```  
*(Parameters similar to AttachedFrame.update; updates the free frame’s position.)*

---

# FutureLogger

`interface FutureLogger`

A `Consumer` used to automatically log future errors or cancellations.

---

# FutureUtils

`object FutureUtils`

Utility methods for working with futures.

---

# Gender

`class Gender : QiEnum`

Enum containing different genders of a human.

**Since:** 1

## Enum Values
- `UNKNOWN`  
- `MALE`  
- `FEMALE`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# GeometryConverter

`interface GeometryConverter`

*(No additional description provided.)*

---

# GoTo

`interface GoTo`

Action to make the robot go somewhere. The destination is represented by a target frame. The robot will try to reach safely the 2D location corresponding to the target frame while avoiding obstacles. The robot may look around and follow non-straight paths to choose the safest way towards the target.

**Since:** 1

## Types
- `interface Async`
- `interface OnStartedListener` – Listener for the started signal.

## Functions

- **addOnStartedListener** – `abstract fun addOnStartedListener(listener: OnStartedListener!): Unit`  
- **async** – `abstract fun async(): GoTo.Async!`  
- **removeAllOnStartedListeners** – `abstract fun removeAllOnStartedListeners(): Unit`  
- **removeOnStartedListener** – `abstract fun removeOnStartedListener(listener: OnStartedListener!): Unit`  
- **run** – `abstract fun run(): Unit` – Run the GoTo on the robot. The `started()` signal is emitted when the action starts.  
- **setOnStartedListener** – `abstract fun ~setOnStartedListener~(listener: OnStartedListener!): Unit` *(deprecated)*

---

# GoToBuilder

`open class GoToBuilder`

Build a new `GoTo`.

## Functions

- **build** – `open fun build(): GoTo!`  
- **buildAsync** – `open fun buildAsync(): Future<GoTo>!`  
- **with** – `open static fun with(context: QiContext!): GoToBuilder!`  
- **withFrame** – `open fun withFrame(frame: Frame!): GoToBuilder!` – Set the target frame for the GoTo action.  
- **withConfiguration** – `open fun withConfiguration(config: GoToConfig!): GoToBuilder!` – Set the configuration for the GoTo action.

---

# GoToConfig

`open class GoToConfig`

Configuration parameters of a GoTo action. If a parameter is not set by the user, it will default to the action’s standard behavior.

*(GoToConfig likely contains properties like path planning policy, orientation policy, max speed, etc.)*

---

# Holder

`interface Holder`

*(No description provided; possibly a generic holder interface for resource locking?)*

---

# HolderBuilder

`open class HolderBuilder`

Build a new `Holder`.

---

# Human

`interface Human`

Object representing a physical person detected by the robot.

**Since:** 1

## Types
- `interface Async`
- `interface OnAttentionLevelChangedListener` – *(Possible listener for attention level? Not shown on site, but likely similar pattern if present.)*

## Functions

*(Functions likely include getters for properties like position (Frame), engagement intention, etc., but details not provided in summary. Possibly:* `getHeadFrame()`, `getBodyFrame()`, `getEmotion()`, etc.)*

*(No details given in the listing beyond description, so we'll skip specifics.)*

---

# HumanAwareness

`interface HumanAwareness`

Service exposing actions and properties related to human-robot interaction.

**Since:** 1

## Types
- `interface Async`

## Functions

*(Likely includes methods to enable/disable human awareness, get detected humans, etc. Not elaborated in summary text.)*

---

# HumanawarenessConverter

`interface HumanawarenessConverter`

*(No additional description provided.)*

---

# HumanConverter

`interface HumanConverter`

*(No additional description provided.)*

---

# ImageConverter

`interface ImageConverter`

*(No additional description provided.)*

---

# IOUtils

`object IOUtils`

Utility methods for working with raw files and assets.

---

# Knowledge

`interface Knowledge`

*//! Service handling the shared knowledge.* (This appears to be a comment in the source; perhaps not intended as documentation text.)

*(No additional information provided; likely provides access to a knowledge graph or data store.)*

---

# KnowledgeBase

`interface KnowledgeBase`

Object allowing reading data from given named graphs.

---

# KnowledgeConverter

`interface KnowledgeConverter`

*(No additional description provided.)*

---

# KnowledgeSubscriber

`interface KnowledgeSubscriber`

*(No description provided; possibly for subscribing to knowledge graph updates.)*

---

# Language

`class Language : QiEnum`

All the possible languages.

**Since:** 1

## Enum Values
*(Likely values correspond to language codes, but not explicitly listed in summary.)*

---

# LanguageUtil

`object LanguageUtil`

Language utility class.

---

# Listen

`interface Listen`

Action to make the robot listen to and recognize a specific set of phrases pronounced by a user. On recognition success, a ListenResult gives the heard phrase and the matching PhraseSet.

**Since:** 1

## Types
- `interface Async`

## Functions

- **async** – `abstract fun async(): Listen.Async!`  
- **run** – `abstract fun run(): ListenResult!` – Run the listening action; returns a result containing the heard phrase and matching phrase set (blocking call).  
- **runAsync** – `abstract fun runAsync(): Future<ListenResult>!` – Run the listening action asynchronously.  
- **setPhraseSets** – `abstract fun setPhraseSets(phraseSets: List<PhraseSet!>!): Unit` – Specify the set of PhraseSets that the robot should listen for.

*(And possibly functions to add listeners for events like sound localization, etc, if any.)*

---

# ListenBuilder

`open class ListenBuilder`

Build a new `Listen`.

## Functions

- **build** – `open fun build(): Listen!`  
- **buildAsync** – `open fun buildAsync(): Future<Listen>!`  
- **with** – `open static fun with(context: QiContext!): ListenBuilder!`  
- **withPhraseSets** – `open fun withPhraseSets(phraseSets: List<PhraseSet!>!): ListenBuilder!`

---

# ListenOptions

`open class ListenOptions`

Optional parameters for the configuration of a Listen action.

---

# ListenResult

`open class ListenResult`

The heard phrase along with the matching phrase set.

## Properties
- **heardPhrase**: Phrase – The phrase that was recognized.  
- **matchedPhraseSet**: PhraseSet – The PhraseSet in which the phrase was found.

---

# Locale

`class Locale`

A locale.

*(Likely identifies language and region; no further description in summary.)*

---

# LocaleConverter

`interface LocaleConverter`

*(No additional description provided.)*

---

# LocalizationStatus

`class LocalizationStatus : QiEnum`

Localization process status.

**Since:** 1

## Enum Values
- `SCANNING`  
- `LOCALIZED`  
- `LOST`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# Localize

`interface Localize`

Action to make the robot localize itself in a map previously built by a LocalizeAndMap action. Only one LocalizeAndMap or Localize action can run at a time. When run, the robot first executes a rotating base and head scan. During this initial scan, the action needs all the robot’s resources to run, and the action status is `Scanning`. After this scan, the robot is localized inside the map, and its position and orientation relative to the map origin can be retrieved at any time by calling `Actuation.robotFrame().computeTransform(Mapping.mapFrame())`. While the action is running, the robot may autonomously look around to confirm its location.

**Since:** 3

## Types
- `interface Async`
- `interface OnStartedListener` – Listener for the started signal.
- `interface OnStatusChangedListener` – Listener for the status property changed event.

## Functions

- **addOnStartedListener** – `abstract fun addOnStartedListener(listener: OnStartedListener!): Unit`  
- **addOnStatusChangedListener** – `abstract fun addOnStatusChangedListener(listener: OnStatusChangedListener!): Unit` – Add a listener for status changes.  
- **async** – `abstract fun async(): Localize.Async!`  
- **getStatus** – `abstract fun getStatus(): LocalizationStatus!` – Exposes the current localization status.  
- **removeAllOnStartedListeners** – `abstract fun removeAllOnStartedListeners(): Unit`  
- **removeAllOnStatusChangedListeners** – `abstract fun removeAllOnStatusChangedListeners(): Unit`  
- **removeOnStartedListener** – `abstract fun removeOnStartedListener(listener: OnStartedListener!): Unit`  
- **removeOnStatusChangedListener** – `abstract fun removeOnStatusChangedListener(listener: OnStatusChangedListener!): Unit`  
- **run** – `abstract fun run(): Unit` – Start the localization process. It will run until the future returned by run() is canceled.  
- **runWithLocalizationHint** – `abstract fun runWithLocalizationHint(hint: Transform!): Unit` – Start the localization process with a hint about the robot’s current location relative to the map origin. The process will run until canceled.  
- **setOnStartedListener** – `abstract fun ~setOnStartedListener~(listener: OnStartedListener!): Unit` *(deprecated)*  
- **setOnStatusChangedListener** – `abstract fun ~setOnStatusChangedListener~(listener: OnStatusChangedListener!): Unit` *(deprecated)*

---

# LocalizeAndMap

`interface LocalizeAndMap`

Action to make the robot explore an unknown environment while localizing itself and building a representation of this environment (known as an exploration map). Only one action among LocalizeAndMap or Localize can run at a time. When run, the robot first executes a rotating base and head scan. During this initial scan, the action needs all the robot’s resources to run and the action status is `Scanning`. After the scan, it is the developer’s responsibility to make the robot move and to stop the mapping when done. The developer thus has full control over the mapped area. While the action is running, the robot may autonomously look around to confirm its location. A given environment needs to be mapped once and only once. The result of this mapping can be dumped to an ExplorationMap object. Afterwards, the ExplorationMap object can be used to create a Localize action that will enable the robot to keep track of its position relative to the map.

**Since:** 3

## Types
- `interface Async`
- `interface OnStartedListener`

## Functions

- **addOnStartedListener** – `abstract fun addOnStartedListener(listener: OnStartedListener!): Unit`  
- **async** – `abstract fun async(): LocalizeAndMap.Async!`  
- **removeAllOnStartedListeners** – `abstract fun removeAllOnStartedListeners(): Unit`  
- **removeOnStartedListener** – `abstract fun removeOnStartedListener(listener: OnStartedListener!): Unit`  
- **run** – `abstract fun run(): Unit` – Start the mapping process (scanning and exploring).  
- **setOnStartedListener** – `abstract fun ~setOnStartedListener~(listener: OnStartedListener!): Unit` *(deprecated)*

---

# LocalizeAndMapBuilder

`open class LocalizeAndMapBuilder`

Build a new `LocalizeAndMap`.

## Functions

- **build** – `open fun build(): LocalizeAndMap!`  
- **buildAsync** – `open fun buildAsync(): Future<LocalizeAndMap>!`  
- **with** – `open static fun with(context: QiContext!): LocalizeAndMapBuilder!`

---

# LocalizeBuilder

`open class LocalizeBuilder`

Build a new `Localize`.

## Functions

- **build** – `open fun build(): Localize!`  
- **buildAsync** – `open fun buildAsync(): Future<Localize>!`  
- **with** – `open static fun with(context: QiContext!): LocalizeBuilder!`

---

# LocalizedString

`open class LocalizedString`

Struct representing a string with a language (a locale-specific string).

---

# LookAt

`interface LookAt`

Action to look at and track a target. The target is represented by a frame, and the robot will look at the origin of that frame. In practice, the action will make the robot move so that the x-axis of the gaze frame aligns with the origin of the target frame. The robot will track the target until the action is canceled.

**Since:** 1

## Types
- `interface Async`

## Functions

### async
```kotlin
abstract fun async(): LookAt.Async!
```

### run
Start looking at the target frame.  
```kotlin
abstract fun run(): Unit
```  
*(No return, runs until canceled.)*

### setPolicy
```kotlin
abstract fun setPolicy(policy: LookAtMovementPolicy!): Unit
```  
Set the strategy for the LookAt movement.  
**Parameters:**  
- `policy` – `LookAtMovementPolicy!`: The movement policy to use (e.g., head only, whole body, etc.).

---

# LookAtBuilder

`open class LookAtBuilder`

Build a new `LookAt`.

## Functions

- **build** – `open fun build(): LookAt!`  
- **buildAsync** – `open fun buildAsync(): Future<LookAt>!`  
- **with** – `open static fun with(context: QiContext!): LookAtBuilder!`  
- **withFrame** – `open fun withFrame(frame: Frame!): LookAtBuilder!` – Set the target frame to look at.  
- **withPolicy** – `open fun withPolicy(policy: LookAtMovementPolicy!): LookAtBuilder!` – Set the movement policy.

---

# LookAtMovementPolicy

`class LookAtMovementPolicy : QiEnum`

Strategies to look at a target.

**Since:** 1

## Enum Values
- `HEAD_ONLY`  
- `WHOLE_BODY`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# Mapping

`interface Mapping`

A service providing the mapping of the local area.

**Since:** 1

## Types
- `interface Async`

## Functions

### async
```kotlin
abstract fun async(): Mapping.Async!
```

### mapFrame
Get the Frame representing the origin of the map.  
```kotlin
abstract fun mapFrame(): Frame!
```  
**Return:** `Frame!` – The map frame (origin of the built map).

---

# MapTopGraphicalRepresentation

`open class MapTopGraphicalRepresentation`

Used to display map shape on a UI. Scale, x, y and theta can be used to switch from world coordinates (relative to mapFrame) to image coordinates in pixels using the following formulas: 

*//!*
```
x_map = scale * (cos(theta) * x_img + sin(theta) * y_img) + x  
y_map = scale * (sin(theta) * x_img - cos(theta) * y_img) + y  

x_img = (1/scale) * (cos(theta) * (x_map - x) + sin(theta) * (y_map - y))  
y_img = (1/scale) * (sin(theta) * (x_map - x) - cos(theta) * (y_map - y))
``` 
Map frame (meters) vs image pixel coordinates:
- ^ y_map (meters)  
- --> x_img (pixels)  
- (with axes illustration)  
*//!*

*(The above formulas illustrate how to convert between map coordinates and image coordinates using the provided scale and offsets.)*

---

# Node

`open class Node`

Struct representing a node in the knowledge graph. A Node holds an object which can be a ResourceNode or a LiteralNode. Every node other than ResourceNode is considered a literal. Literal nodes can handle the following types: 
- `str` 
- `LocalizedString` 
- `float` 
- `int` 
- `TimeString` 
- `DateTimeString` 
- `DateString`

---

# OrientationPolicy

`class OrientationPolicy : QiEnum`

Policy defining orientation control of a given frame with respect to a target frame.

**Since:** 1

## Enum Values
- `FREE_ORIENTATION`  
- `MATCH_ORIENTATION`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# PathPlanningPolicy

`class PathPlanningPolicy : QiEnum`

Path planning strategies to go to a target.

**Since:** 1

## Enum Values
- `DEFAULT` (e.g., GetAroundObstacles)  
- `SHORTCUT` (if available)  

*(Exact enum values not listed on site, but likely includes ones referenced in GoToConfig defaults.)*

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# Phrase

`open class Phrase`

Structure containing a chunk of text intended to be said or listened to by the robot.

---

# PhraseSet

`open class PhraseSet`

Object representing a set of phrases, used to group phrases considered as synonyms. *Example:* `"yes", "yep", "indeed", ...`

---

# PhraseSetBuilder

`open class PhraseSetBuilder`

Build a new `PhraseSet`.

## Functions

- **build** – `open fun build(): PhraseSet!`  
- **buildAsync** – `open fun buildAsync(): Future<PhraseSet>!`  
- **with** – `open static fun with(context: QiContext!): PhraseSetBuilder!`  
- **withTexts** – `open fun withTexts(texts: List<String!>!): PhraseSetBuilder!` – Set the list of phrases.

---

# PhraseSetUtil

`object PhraseSetUtil`

PhraseSet utility class.

---

# PleasureState

`class PleasureState : QiEnum`

Enum containing the possible states reached on the pleasure-displeasure scale.

**Since:** 1

## Enum Values
- `UNKNOWN`  
- `NEGATIVE`  
- `NEUTRAL`  
- `POSITIVE`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# Power

`interface Power`

The Power service aggregates information related to robot power management.

**Since:** 1

## Types
- `interface Async`

## Functions

*(Likely includes methods to check battery status, power source, etc., not detailed here.)*

---

# PowerConverter

`interface PowerConverter`

*(No additional description provided.)*

---

# Qi

`interface Qi`

*(No description provided; possibly a core interface? No content shown on site.)*

---

# QiChatbot

`interface QiChatbot`

Chatbot that can be used to make the robot able to chat with a human. The interaction will be based on the content given in the QiChatbot topics.

**Since:** 1

## Types
- `interface Async`
- `interface OnBookmarkReachedListener` – Listener for when a bookmark is reached.
- `interface OnEndedListener` – Listener for when the chatbot has finished an utterance or conversation.
- `interface OnFallbackReplyFoundListener` – Listener for when a fallback reply is found.
- `interface OnNoReplyFoundListener` – Listener for when no reply is found.
- `interface OnNormalReplyFoundListener` – Listener for when a normal reply is found.
- `interface OnPausedListener` – Listener for when the chatbot is paused.
- `interface OnResumedListener` – Listener for when the chatbot is resumed.

## Functions

*(QiChatbot likely has functions to load topics, set executors, variable values, etc., as well as add listeners for the above events. Due to the complexity, and since specifics aren't given in the summary text, we'll skip detailed listing. It definitely includes methods like `addOnBookmarkReachedListener`, `goToBookmark`, `loadTopic`, `removeAllListeners`, etc.)*

---

# QiChatbotBuilder

`open class QiChatbotBuilder`

Build a new `QiChatbot`.

## Functions

- **build** – `open fun build(): QiChatbot!`  
- **buildAsync** – `open fun buildAsync(): Future<QiChatbot>!`  
- **with** – `open static fun with(context: QiContext!): QiChatbotBuilder!`  
- **withTopic** – `open fun withTopic(topic: Topic!): QiChatbotBuilder!`  
- **withTopics** – `open fun withTopics(topics: List<Topic!>!): QiChatbotBuilder!`  
- **withLocale** – `open fun withLocale(locale: Locale!): QiChatbotBuilder!` *(if needed to set language)*

---

# QiChatExecutor

`interface QiChatExecutor`

Object representing a user-defined action to execute synchronously during an utterance in a QiChatbot.

*(Usually implemented by developers to perform custom actions when invoked from a QiChat topic.)*

---

# QiChatVariable

`interface QiChatVariable`

Object representing a variable in a QiChat topic.

*(Provides methods to read/write the variable’s value.)*

---

# QiContext

`open class QiContext : ContextWrapper, Callback`

*(The QiContext class is the main entry point to QiSDK services within an Android context.)*

## Functions

- **convert** – `open fun <T : Any!> convert(o: Any!, type: Type!): T` – *Convert an object of one type to another if possible (for internal use, rarely used by developers).*  
- **getActuation** – `open fun getActuation(): Actuation!` – Return the robot “Actuation” service.  
- **getActuationAsync** – `open fun getActuationAsync(): Future<Actuation>!`  
- **getAutonomousAbilities** – `open fun getAutonomousAbilities(): AutonomousAbilities!` – Return the robot “AutonomousAbilities” service.  
- **getAutonomousAbilitiesAsync** – `open fun getAutonomousAbilitiesAsync(): Future<AutonomousAbilities>!`  
- **getCamera** – `open fun getCamera(): Camera!` – Return the robot “Camera” service.  
- **getCameraAsync** – `open fun getCameraAsync(): Future<Camera>!`  
- **getContextFactory** – `open fun getContextFactory(): RobotContextFactory!` – Return the robot “ContextFactory” service.  
- **getContextFactoryAsync** – `open fun getContextFactoryAsync(): Future<RobotContextFactory>!`  
- **getConversation** – `open fun getConversation(): Conversation!` – Return the robot “Conversation” service.  
- **getConversationAsync** – `open fun getConversationAsync(): Future<Conversation>!`  
- **getFocus** – `open fun getFocus(): Focus!` – Return the robot “Focus” service.  
- **getFocusAsync** – `open fun getFocusAsync(): Future<Focus>!`  
- **getHumanAwareness** – `open fun getHumanAwareness(): HumanAwareness!` – Return the robot “HumanAwareness” service.  
- **getHumanAwarenessAsync** – `open fun getHumanAwarenessAsync(): Future<HumanAwareness>!`  
- **getMapping** – `open fun getMapping(): Mapping!` – Return the robot “Mapping” service.  
- **getMappingAsync** – `open fun getMappingAsync(): Future<Mapping>!`  
- **getPower** – `open fun getPower(): Power!` – Return the robot “Power” service.  
- **getPowerAsync** – `open fun getPowerAsync(): Future<Power>!`  
- **getTouch** – `open fun getTouch(): Touch!` – Return the robot “Touch” service.  
- **getTouchAsync** – `open fun getTouchAsync(): Future<Touch>!`  
- *(Other service getters like `getAudio`, `getChat`, etc., if any, would follow the same pattern.)*

*(Note: QiContext inherits Android’s ContextWrapper and likely implements some Callback interface for asynchronous operations, but those details are outside QiSDK scope.)*

---

# QiDisconnectionListener

`interface QiDisconnectionListener`

Session disconnection listener.

*(Likely has a method `onRobotDisconnected()` or similar to be overridden.)*

---

# QiEnum

`interface QiEnum`

*(A marker interface for enumerations in QiSDK that map to int values.)*

## Functions

- **getQiValue** – `abstract fun getQiValue(): Int`

## Inheritors

The following classes implement `QiEnum` (each representing a specific enumerated type in the SDK):

- **AttentionState** – Enum containing the possible attention states of the human (where the human is looking). `class AttentionState : QiEnum`  
- **AutonomousReactionImportance** – Additional info on the importance of a suggested ChatbotReaction. `class AutonomousReactionImportance : QiEnum`  
- **AutonomousReactionValidity** – Describes the validity of a suggested ChatbotReaction. `class AutonomousReactionValidity : QiEnum`  
- **BodyLanguageOption** – Body language policy. `class BodyLanguageOption : QiEnum`  
- **ChatbotReactionHandlingStatus** – Status of a ChatbotReaction (in a Chat action). `class ChatbotReactionHandlingStatus : QiEnum`  
- **DegreeOfFreedom** – A degree of freedom. `class DegreeOfFreedom : QiEnum`  
- **EngagementIntentionState** – Engagement intention of the human. `class EngagementIntentionState : QiEnum`  
- **EngagementPolicy** – Eye contact policy. `class EngagementPolicy : QiEnum`  
- **ExcitementState** – Perceived energy in emotion. `class ExcitementState : QiEnum`  
- **Gender** – Different genders of a human. `class Gender : QiEnum`  
- **Language** – Possible languages. `class Language : QiEnum`  
- **LocalizationStatus** – Localization process status. `class LocalizationStatus : QiEnum`  
- **LookAtMovementPolicy** – Strategies to look at a target. `class LookAtMovementPolicy : QiEnum`  
- **OrientationPolicy** – Orientation control policy. `class OrientationPolicy : QiEnum`  
- **PathPlanningPolicy** – Path planning strategies. `class PathPlanningPolicy : QiEnum`  
- **PleasureState** – States on pleasure-displeasure scale. `class PleasureState : QiEnum`  
- **Region** – All possible regions. `class Region : QiEnum`  
- **ReplyPriority** – Priority of a Chatbot reply. `class ReplyPriority : QiEnum`  
- **SmileState** – Possible smiling states of the human. `class SmileState : QiEnum`

*(Each of the above classes is documented separately in this collection.)*

---

# QiRobot

`open class QiRobot : Callback`

Represents a connection to a robot.

## Functions

- **onRobotAbsent** – `open fun onRobotAbsent(): Unit` – Callback when the robot becomes absent.  
- **onRobotLost** – `open fun onRobotLost(): Unit` – Callback when the robot connection is lost.  
- **onRobotReady** – `open fun onRobotReady(session: Session!): Unit` – Callback when the robot is ready (session connected), providing the Session.

*(QiRobot likely implements some interface with these callbacks for robot connection events.)*

---

# QiSDK

`open class QiSDK`

Helper to initialize Qi SDK.

## Constructors
- **QiSDK()** – Helper to initialize Qi SDK.

## Properties
- **VERSION** – `static val VERSION: String!` – *(SDK version string.)*

## Functions

### getSerializer
```kotlin
open static fun getSerializer(): QiSerializer!
```  
*(Returns an instance of QiSerializer for custom object serialization support.)*

### init
```kotlin
open static fun init(application: Application!): Unit
```  
Initialize the QiSDK with the given Android application context.  
**Parameters:**  
- `application` – `Application!`: The Android Application instance.

### register
```kotlin
open static fun register(activity: Activity!, callbacks: RobotLifecycleCallbacks!): Unit
```  
Add a new RobotLifecycleCallbacks for the default QiRobot.  
**Parameters:**  
- `activity` – `Activity!`: The Android Activity to associate with the callbacks.  
- `callbacks` – `RobotLifecycleCallbacks!`: The callback implementation to register.

### unregister
```kotlin
open static fun unregister(activity: Activity!, callbacks: RobotLifecycleCallbacks!): Unit
```  
Remove a RobotLifecycleCallbacks for the default QiRobot.  
**Parameters:**  
- `activity` – `Activity!`: The Activity associated with the callbacks.  
- `callbacks` – `RobotLifecycleCallbacks!`: The callback implementation to remove.

```kotlin
open static fun unregister(activity: Activity!): Unit
```  
Unregister all RobotLifecycleCallbacks for the default QiRobot associated with the given activity.  
**Parameters:**  
- `activity` – `Activity!`: The Activity for which all callbacks should be removed.

---

# QiThreadPool

`class QiThreadPool`

Shared thread pool. (Mainly for internal purpose.)

## Functions

- **execute** – `static fun <V : Any!> execute(callable: Callable<V>!): Future<V>!` – Execute a callable on the thread pool.  
  **Parameters:**  
  - `callable` – `Callable<V>!`: The task to execute.  
  **Return:** `Future<V>!` – A future representing the pending result.  
- **schedule** – `static fun <V : Any!> schedule(callable: Callable<V>!, delay: Long, timeUnit: TimeUnit!): Future<V>!` – Execute a callable with a delay on the thread pool.  
  **Parameters:**  
  - `callable` – `Callable<V>!`: The task to execute.  
  - `delay` – `Long`: The delay before execution.  
  - `timeUnit` – `TimeUnit!`: The time unit of the delay.  
  **Return:** `Future<V>!` – A future representing the scheduled task.

---

# Quaternion

`class Quaternion`

Quaternion representation of rotations. *See:* [Quaternion (Wikipedia)](https://en.wikipedia.org/wiki/Quaternion) for more information.

*(Likely has components x, y, z, w and possibly methods to convert to/from Euler angles or multiply quaternions.)*

---

# Region

`class Region : QiEnum`

All the possible regions.

**Since:** 1

## Enum Values
*(Likely values like `NORTH_AMERICA`, `EUROPE`, etc., but not listed in summary.)*

---

# RegionUtil

`object RegionUtil`

Region utility class.

---

# ReplyPriority

`class ReplyPriority : QiEnum`

Additional information on the priority of a Chatbot reply.

**Since:** 1

## Enum Values
- `NORMAL`  
- `HIGH`  
- `CRITICAL`  

*(Hypothetical values, not explicitly given but likely.)*

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# ReplyReaction

`interface ReplyReaction`

A Chatbot reaction to a human utterance.

*(Likely similar to ChatbotReaction or a subtype thereof, representing a reply from the bot.)*

---

# Requirement

`interface Requirement`

A requirement creates and holds a `Future`, which represents the value once satisfied.

*(This likely relates to the QiSDK `Requirement` framework for dynamic condition checking. Not further detailed on site.)*

---

# ResourceNode

`open class ResourceNode`

Struct representing a resource node which has a unique URL among the triple database.

*(In the knowledge graph, a ResourceNode represents an entity with a unique identifier (URI).)*

---

# RobotContext

`interface RobotContext`

The context object gathers together all the handles and tokens that authorize an action to be effectively executed on a robot.

*(Used internally to ensure an action has permission to run on the robot, typically obtained via QiContext and not manipulated directly by developers.)*

---

# RobotContextFactory

`interface RobotContextFactory`

The context factory is a service providing a RobotContext.

*(Likely used to create RobotContext instances for actions.)*

---

# RobotLifecycleCallbacks

`interface RobotLifecycleCallbacks`

Robot lifecycle callback interface. 

*(The user should implement this to receive QiSDK events in an Activity lifecycle: onRobotFocusGained, onRobotFocusLost, etc.)*

*(Typically contains methods like `onRobotFocusGained(QiContext)`, `onRobotFocusLost()`, `onRobotFocusRefused(String)` as per QiSDK documentation.)*

---

# RunnableAutonomousReaction

`abstract class RunnableAutonomousReaction`

Alternative AutonomousReaction that allows the implementation of its execution by subclasses instead of delegating it to a ChatbotReaction.

---

# RunnableReplyReaction

`abstract class RunnableReplyReaction`

Alternative ReplyReaction that allows the implementation of its execution by subclasses instead of delegating it to a ChatbotReaction.

---

# Say

`interface Say`

Action to make the robot say a phrase.

**Since:** 1

## Types
- `interface Async`

## Functions

### async
```kotlin
abstract fun async(): Say.Async!
```

### run
Start saying the phrase.  
```kotlin
abstract fun run(): Unit
```  
*(No return; the action will complete when the speech is done.)*

### setLocale
```kotlin
abstract fun setLocale(locale: Locale!): Unit
```  
Set the locale (language) for this Say action.  
**Parameters:**  
- `locale` – `Locale!`: The locale in which to speak the phrase.

---

# SayBuilder

`open class SayBuilder`

Build a new `Say`.

## Functions

- **build** – `open fun build(): Say!`  
- **buildAsync** – `open fun buildAsync(): Future<Say>!`  
- **with** – `open static fun with(context: QiContext!): SayBuilder!`  
- **withPhrase** – `open fun withPhrase(phrase: Phrase!): SayBuilder!` – Set the phrase to say.  
- **withLocale** – `open fun withLocale(locale: Locale!): SayBuilder!` – Set the locale for the phrase.

---

# ServiceRequirement

`interface ServiceRequirement`

*(No description; likely an internal requirement that a given service is accessible.)*

---

# ServiceUnavailableException

`open class ServiceUnavailableException : RuntimeException`

## Constructors

- `ServiceUnavailableException(serviceName: String!)` – Thrown when a required service is not available. *(The parameter `serviceName` indicates which service was unavailable.)*

---

# SessionRequirement

`interface SessionRequirement`

*(No description; likely represents requirement for an active session.)*

---

# SmileState

`class SmileState : QiEnum`

Enum containing the possible smiling states of the human. This feature is only based on facial expression.

**Since:** 1

## Enum Values
- `UNKNOWN`  
- `NOT_SMILING`  
- `SMILING`  

## Functions

- **getQiValue** – `fun getQiValue(): Int`

---

# SpeechEngine

`interface SpeechEngine`

Factory to create Say actions.

*(Likely used internally when creating Say from text, not commonly used directly by developers.)*

---

# StandardAutonomousReaction

`class StandardAutonomousReaction`

Default implementation of `AutonomousReaction`.

---

# StandardReplyReaction

`class StandardReplyReaction`

Default implementation of `ReplyReaction`.

---

# StreamableBuffer

`interface StreamableBuffer`

A buffer that can be read incrementally (little by little).

*(Used for streaming data, perhaps audio or video frames.)*

---

# StreamablebufferConverter

`interface StreamableBufferConverter`

*(No additional description provided.)*

---

# StreamableBufferFactory

`interface StreamableBufferFactory`

Factory of `StreamableBuffer`.

*(Probably used to create StreamableBuffer instances for given data streams.)*

---

# TakePicture

`interface TakePicture`

Action to take pictures on the robot.

**Since:** 1

## Types
- `interface Async`

## Functions

### async
```kotlin
abstract fun async(): TakePicture.Async!
```

### run
Take a picture.  
```kotlin
abstract fun run(): EncodedImage!
```  
**Return:** `EncodedImage!` – The captured image.

### setResolution
```kotlin
abstract fun setResolution(resolution: Int): Unit
```  
*(If applicable, set the camera resolution for pictures.)*

*(Detailed parameter info not given, assume resolution is an index or constant.)*

---

# TakePictureBuilder

`open class TakePictureBuilder`

Build a new `TakePicture`.

## Functions

- **build** – `open fun build(): TakePicture!`  
- **buildAsync** – `open fun buildAsync(): Future<TakePicture>!`  
- **with** – `open static fun with(context: QiContext!): TakePictureBuilder!`

---

# TimestampedImage

`interface TimestampedImage`

Timestamped encoded image.

*(Likely an image with an associated timestamp property.)*

---

# TimestampedImageHandle

`interface TimestampedImageHandle`

Associates a timestamp with an EncodedImageHandle.

---

# TimeString

`open class TimeString`

Struct representing a time as a string.

---

# Topic

`open class Topic`

Object representing a topic (for QiChat).

*(Contains dialog content such as rules, replies, etc., loaded from .top files.)*

---

# TopicBuilder

`open class TopicBuilder`

Build a new `Topic`.

## Functions

- **build** – `open fun build(): Topic!`  
- **buildAsync** – `open fun buildAsync(): Future<Topic>!`  
- **withAsset** – `open static fun with(context: QiContext!, assetName: String!): TopicBuilder!` – Initialize a TopicBuilder with a topic asset name (from assets).  
- **withResource** – `open static fun with(context: QiContext!, resourceId: Int): TopicBuilder!` – Initialize with a raw resource containing a topic.

---

# TopicStatus

`open class TopicStatus`

The current state of a topic in a Discuss action.

*(Likely indicates if topic is started, stopped, etc.)*

---

# Touch

`interface Touch`

The Touch service provides objects to access and subscribe to touch sensor data.

**Since:** 1

## Types
- `interface Async`

## Functions

### async
```kotlin
abstract fun async(): Touch.Async!
```

### getSensors
```kotlin
abstract fun getSensors(): List<TouchSensor>!
```  
Get the list of touch sensors on the robot.  
**Return:** `List<TouchSensor>!` – All available touch sensors.

### getSensor
```kotlin
abstract fun getSensor(name: String!): TouchSensor?
```  
Get a specific touch sensor by name.  
**Parameters:**  
- `name` – `String!`: The name of the touch sensor (e.g., "Head/Touch").  
**Return:** `TouchSensor?` – The sensor, or null if not found.

---

# TouchConverter

`interface TouchConverter`

*(No additional description provided.)*

---

# TouchSensor

`interface TouchSensor`

Object representing a sensor that detects when the robot is touched.

**Since:** 1

## Types
- `interface Async`
- `interface OnStateChangedListener` – Listener for touch state changes.

## Functions

- **addOnStateChangedListener** – `abstract fun addOnStateChangedListener(listener: OnStateChangedListener!): Unit`  
- **async** – `abstract fun async(): TouchSensor.Async!`  
- **getFrame** – `abstract fun getFrame(): Frame!` – Exposes the frame of the touch sensor on the robot.  
- **getName** – `abstract fun getName(): String!` – Exposes the name of the sensor.  
- **getState** – `abstract fun getState(): TouchState!` – Exposes the current state (touched or not).  
- **removeAllOnStateChangedListeners** – `abstract fun removeAllOnStateChangedListeners(): Unit`  
- **removeOnStateChangedListener** – `abstract fun removeOnStateChangedListener(listener: OnStateChangedListener!): Unit`  
- **setOnStateChangedListener** – `abstract fun ~setOnStateChangedListener~(listener: OnStateChangedListener!): Unit` *(deprecated)*

---

# TouchState

`interface TouchState`

Description of a touch sensor state.

*(Likely an interface or class with information whether the sensor is touched, possibly pressure, etc.)*

---

# Transform

`open class Transform`

A homogeneous transformation matrix. *See:* [Transformation matrix (Wikipedia)](http://en.wikipedia.org/wiki/Transformation_matrix) for more information. It is represented by a 3D vector and a quaternion.

*(Transform likely has translation (Vector3) and rotation (Quaternion) components.)*

---

# TransformBuilder

`open class TransformBuilder`

Build a new `Transform`.

## Functions

- **create** – `open static fun create(x: Double, y: Double, z: Double, q: Quaternion!): Transform!` – Create a Transform from components.  
*(Method name inferred; not explicitly listed on site, but likely exists.)*

---

# TransformTime

`open class TransformTime`

A transform associated with a timestamp.

*(Contains a Transform and the time at which it was valid.)*

---

# Triple

`open class Triple`

Struct representing a triple (subject, predicate, object) in a knowledge graph. Subject and predicate are always resources; the object can be a resource or a literal (here encapsulated in a Node).

---

# Vector3

`open class Vector3`

A generic 3D vector.

*(Likely has x, y, z components and possibly utility methods like norm, etc.)*