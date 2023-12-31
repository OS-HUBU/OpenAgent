<h1 align="center" style="margin:30px 0 30px;font-weight:bold">OpenAgent</h1>
<h4 align="center">基于Repast S框架的面向新冠疫情的多智能体时空模拟与可视化仿真框架</h4>

## 平台简介
新冠疫情是由新型冠状病毒(COVID-19)引起的全球性传染病，对世界经济、医疗卫生、社会生活都产生严重影响。目前很多基于新冠疫情的多智能体仿真建模研究没有考虑时空演化对新冠疫情传播的影响，本研究对经典的SEIR传染病动力学模型进行扩展，将时空交互过程与新冠疫情传播机理相结合，借助多智能体和时空可视化技术，设计并实现了一种面向新冠疫情的多智能体时空演化框架。本文以武汉市硚口区作为研究区域，采用真实新冠疫情数据，进行了系列仿真实验，模拟了时空演化、戴口罩率、疫苗接种率、医疗条件等因素对疫情传播的影响。实验证明，本文成果能为新冠传播机理的研究人员提供更直观高效的方法框架。本项目是前后端分离项目

## 内置功能
1. 自定义模拟对象信息：用户可以定义固定的模拟数量（包括健康的人数和潜伏者人数）、同时也可以规定初始化人数的青年占比和老年占比。
2. 自定义当地信息：用户可以自定义当地的口罩率和疫苗率。
3. 自定义时间步长：用户可以自定义智能体的时间步长。
4. 数据可视化:本平台提供了智能体在运行中的数据可视化。
5. 提供两种运行模式：本平台提供两种运行模式，1种是显示智能体的轨迹的运行。2是不显示轨迹的运行

## 在线体验 
http://139.219.132.124:8080/

## 演示图
<table>
  <tr>
    <td>
      <img src="https://agentyou.oss-cn-beijing.aliyuncs.com/1.png"></img>
    </td>
      <td>
      <img src="https://agentyou.oss-cn-beijing.aliyuncs.com/2.png"></img>
    </td>
  </tr>
  <tr>
    <td>
      <img src="https://agentyou.oss-cn-beijing.aliyuncs.com/3.png"></img>
    </td>
  <td><img src="https://agentyou.oss-cn-beijing.aliyuncs.com/4.png"></img></td>
  </tr>
</table>
